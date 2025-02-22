package cytomine.core.ontology

/*
* Copyright (c) 2009-2017. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import cytomine.core.AnnotationDomain
import cytomine.core.CytomineDomain
import cytomine.core.command.*
import cytomine.core.image.ImageInstance
import cytomine.core.project.Project
import cytomine.core.security.SecUser
import cytomine.core.utils.JSONUtils
import cytomine.core.utils.ModelService
import cytomine.core.utils.Task
import com.vividsolutions.jts.geom.Geometry
import groovy.sql.Sql
import org.springframework.security.acls.domain.BasePermission

import static org.springframework.security.acls.domain.BasePermission.READ
import static org.springframework.security.acls.domain.BasePermission.WRITE

class PropertyService extends ModelService {

    static transactional = true
    def cytomineService
    def transactionService
    def dataSource
    def securityACLService

    def currentDomain() {
        return Property;
    }

    def list() {
        securityACLService.checkAdmin(cytomineService.currentUser)
        return Property.list()
    }

    def list(CytomineDomain cytomineDomain) {
        if(!cytomineDomain.class.name.contains("AbstractImage")) {
            securityACLService.check(cytomineDomain.container(), BasePermission.READ)
        }
        Property.findAllByDomainIdent(cytomineDomain.id)
    }

    List<String> listKeysForAnnotation(Project project, ImageInstance image, Boolean withUser) {
        if (project != null)
            securityACLService.check(project, BasePermission.READ)
        else
            securityACLService.check(image.container(), BasePermission.READ)

        String request = "SELECT DISTINCT p.key " +
                (withUser? ", ua.user_id " : "") +
                "FROM property as p, user_annotation as ua " +
                "WHERE p.domain_ident = ua.id " +
                (project? "AND ua.project_id = '"+ project.id + "' " : "") +
                (image? "AND ua.image_id = '"+ image.id + "' " : "") +
                "UNION " +
                "SELECT DISTINCT p1.key " +
                (withUser? ", aa.user_id " : "") +
                "FROM property as p1, algo_annotation as aa " +
                "WHERE p1.domain_ident = aa.id " +
                (project? "AND aa.project_id = '"+ project.id + "' " : "") +
                (image? "AND aa.image_id = '"+ image.id + "' " : "") +
                "UNION " +
                "SELECT DISTINCT p2.key " +
                (withUser? ", ra.user_id " : "") +
                "FROM property as p2, reviewed_annotation as ra " +
                "WHERE p2.domain_ident = ra.id " +
                (project? "AND ra.project_id = '"+ project.id + "' " : "") +
                (image? "AND ra.image_id = '"+ image.id + "' " : "")

        return  (withUser ? selectListKeyWithUser(request) : selectListkey(request))
    }

     List<String> listKeysForImageInstance(Project project) {
        if (project != null)
            securityACLService.check(project, BasePermission.READ)

        String request = "SELECT DISTINCT p.key " +
                "FROM property as p, image_instance as ii " +
                "WHERE p.domain_ident = ii.id " +
                "AND ii.project_id = "+ project.id;

        return selectListkey(request)
    }

    def listAnnotationCenterPosition(SecUser user, ImageInstance image, Geometry boundingbox, String key) {
        securityACLService.check(image.container(), BasePermission.READ)
        String request = "SELECT DISTINCT ua.id, ST_X(ST_CENTROID(ua.location)) as x,ST_Y(ST_CENTROID(ua.location)) as y, p.value " +
                "FROM user_annotation ua, property as p " +
                "WHERE p.domain_ident = ua.id " +
                "AND p.key = '"+ key + "' " +
                "AND ua.image_id = '"+ image.id +"' " +
                "AND ua.user_id = '"+ user.id +"' " +
                (boundingbox ? "AND ST_Intersects(ua.location,ST_GeometryFromText('" + boundingbox.toString() + "',0)) " :"") +
                "UNION " +
                "SELECT DISTINCT aa.id, ST_X(ST_CENTROID(aa.location)) as x,ST_Y(ST_CENTROID(aa.location)) as y, p.value " +
                "FROM algo_annotation aa, property as p " +
                "WHERE p.domain_ident = aa.id " +
                "AND p.key = '"+ key + "' " +
                "AND aa.image_id = '"+ image.id +"' " +
                "AND aa.user_id = '"+ user.id +"' " +
                (boundingbox ? "AND ST_Intersects(aa.location,ST_GeometryFromText('" + boundingbox.toString() + "',0)) " :"")

        return selectsql(request)
    }

    def read(def id) {
        def property = Property.read(id)
        if (property && !property.domainClassName.contains("AbstractImage") && !property.domainClassName.contains("Software")) {
            securityACLService.check(property.container(), BasePermission.READ)
        }
        property
    }

    def read(CytomineDomain domain, String key) {
        def property = Property.findByDomainIdentAndKey(domain.id,key)
        if (property && !property.domainClassName.contains("AbstractImage") && !property.domainClassName.contains("Software")) {
            securityACLService.check(property.container(), BasePermission.READ)
        }
        property
    }

    def add(def json, def transaction = null) {
        def domainClass = json.domainClassName
        CytomineDomain domain

        if(domainClass.contains("AnnotationDomain")) {
            domain = AnnotationDomain.getAnnotationDomain(json.domainIdent)
        } else {
            domain = Class.forName(domainClass, false, Thread.currentThread().contextClassLoader).read(JSONUtils.getJSONAttrLong(json,'domainIdent',0))
        }

        if (domain != null && !domain.class.name.contains("AbstractImage")) {
            securityACLService.check(domain.container(), BasePermission.READ)
            if (domain.hasProperty('user') && domain.user) {
                securityACLService.checkFullOrRestrictedForOwner(domain, domain.user)
            } else {
                securityACLService.checkisNotReadOnly(domain)
            }
        }

        SecUser currentUser = cytomineService.getCurrentUser()
        Command command = new AddCommand(user: currentUser, transaction: transaction)
        return executeCommand(command,null,json)
    }

    def afterAdd(def domain, def response) {
        Property property = (Property) domain
        addDefaultColor(property)
    }

    def addDefaultColor(Property property) {
        if(property.key.equals("ANNOTATION_GROUP_ID")){
            Long id = -1
            try {
                id = Long.parseLong(property.value)
            } catch (NumberFormatException e){
                e.printStackTrace()
            }
            if(id == -1) return;

            def colors = ["#e6194b",	//	Red
                          "#3cb44b",	//	Green
                          "#ffe119",	//	Yellow
                          "#0082c8",	//	Blue
                          "#f58231",	//	Orange
                          "#911eb4",	//	Purple
                          "#46f0f0",	//	Cyan
                          "#f032e6",	//	Magenta
                          "#d2f53c",	//	Lime
                          "#fabebe",	//	Pink
                          "#008080",	//	Teal
                          "#e6beff",	//	Lavender
                          "#aa6e28",	//	Brown
                          "#fffac8",	//	Beige
                          "#800000",	//	Maroon
                          "#aaffc3",	//	Mint
                          "#808000",	//	Olive
                          "#ffd8b1",	//	Coral
                          "#000080",	//	Navy
                          "#808080",	//	Grey
                          "#FFFFFF",	//	White
                          "#000000"	//	Black
            ]

            String color = colors[(int) (id%(colors.size()))]

            //add CUSTOM_ANNOTATION_DEFAULT_COLOR property
            Property defaultColor = new Property(domainClassName: property.domainClassName, domainIdent: property.domainIdent, key:"CUSTOM_ANNOTATION_DEFAULT_COLOR", value: color);
            create(defaultColor,false)
        }
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(Property ap, def jsonNewData) {
        if(!ap.domainClassName.contains("AbstractImage")) {
            securityACLService.check(ap.container(), BasePermission.READ)
            if (ap.retrieveCytomineDomain().hasProperty('user') && ap.retrieveCytomineDomain().user) {
                securityACLService.checkFullOrRestrictedForOwner(ap, ap.retrieveCytomineDomain().user)
            } else {
                securityACLService.checkisNotReadOnly(ap)
            }
        }

        SecUser currentUser = cytomineService.getCurrentUser()
        Command command = new EditCommand(user: currentUser)
        return executeCommand(command,ap,jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(Property domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        if(!domain.domainClassName.contains("AbstractImage")) {
            securityACLService.check(domain.container(), BasePermission.READ)
            if (domain.retrieveCytomineDomain().hasProperty('user') && domain.retrieveCytomineDomain().user) {
                securityACLService.checkFullOrRestrictedForOwner(domain, domain.retrieveCytomineDomain().user)
            } else if (domain.domainClassName.contains("Project")){
                securityACLService.check(domain.domainIdent,domain.domainClassName, BasePermission.WRITE)
            } else {
                securityACLService.checkisNotReadOnly(domain)
            }
        }
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.key, domain.domainClassName, domain.domainIdent]
    }

    private def selectListkey(String request) {
        def data = []
        def sql = new Sql(dataSource)
        sql.eachRow(request) {
            String key = it[0]
            data << key
        }
        try {
            sql.close()
        }catch (Exception e) {}
        data
    }

    private def selectListKeyWithUser(String request) {
        def data = []
        def sql = new Sql(dataSource)
        sql.eachRow(request) {
            String key = it[0]
            String user = it[1]
            data << [key : key, userId : user]
        }
        try {
            sql.close()
        }catch (Exception e) {}
        data
    }

    private def selectsql(String request) {
        def data = []
        def sql = new Sql(dataSource)
         sql.eachRow(request) {

            long idAnnotation = it[0]
            String value = it[3]

            data << [idAnnotation: idAnnotation, x: it[1],y: it[2], value: value]
        }
        try {
            sql.close()
        }catch (Exception e) {}
        data
    }
}
