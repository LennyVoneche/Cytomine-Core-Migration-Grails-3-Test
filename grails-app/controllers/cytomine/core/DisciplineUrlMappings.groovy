package cytomine.core
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

/**
 * Cytomine
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class DisciplineUrlMappings {

    static mappings = {
        "/api/discipline.$format"(controller:"restDiscipline"){
            action = [GET: "list",POST:"add"]
        }
        "/api/discipline/$id.$format"(controller:"restDiscipline"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
    }
}
