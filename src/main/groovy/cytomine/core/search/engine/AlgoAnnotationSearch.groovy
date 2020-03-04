package cytomine.core.search.engine

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

import cytomine.core.ontology.AlgoAnnotation

/**
 * Created by lrollus on 7/22/14.
 */
class AlgoAnnotationSearch extends AnnotationSearch {

    public String getClassName() {
        return AlgoAnnotation.class.name
    }

    public String getTable() {
        return "algo_annotation"
    }

    public String getTermTable() {
        return "algo_annotation_term"
    }

    public String getLinkTerm() {
        return "AND annotation.id = at.annotation_ident\n" +
                "AND term.id = at.term_id"
    }

}
