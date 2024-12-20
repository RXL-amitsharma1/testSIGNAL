/* Copyright 2010 Mihai Cazacu
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
package com.rxlogix.attachments

class AttachmentLink {

    String referenceClass
    Long referenceId

    static hasMany = [attachments: Attachment]

    static constraints = {
        referenceClass blank: false
        referenceId min: 0L
    }

    static mapping = {
        cache true
    }

    def getReference() {
        getClass().classLoader.loadClass(referenceClass).get(referenceId)
    }

}