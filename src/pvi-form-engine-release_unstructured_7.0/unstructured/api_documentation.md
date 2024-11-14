# Unstructured Extraction API

API for processing text narratives and extracting entities.

## Servers

- **URL:** The URL for the server where the application is currently deployed.
- **URL:** For adding URL of application server. Add URL without end /
  - **appUrl:** url for localhost (Default: [http://127.0.0.1:9888](http://127.0.0.1:9888))

## Paths

### /docs

- **GET:**
  - **Summary**: Render API Documentation
  - **Responses**:
    - **`200`**: Description: API Documentation HTML page

### /unstruct/live

- **POST:**
  - Summary: Process text narratives with models and gives entities
    - Request Body:
      - Content Type: `application/x-www-form-urlencoded`
        - Schema:
          ```yaml
          type: object
          properties:
            text:
              type: string
            language:
              type: string
          ```
        - Examples:
            - Narrative Example:
              - Value:
                ```yaml
                text: "Case narrative....."
                langauge: "en"
                ```
    - Responses:  
      - **`200`:** Successful operation   
        - Content Type: `application/json`  
          - Schema:  
            ```json
              {
                "senderCaseUid": "integer",
                "senderCaseUid_acc": "integer",
                "senderCaseVersion": "string",
                "senderCaseVersion_acc": "integer",
                "summary": {
                  "caseDescription": "string",
                  "caseDescription_acc": "integer",
                  "senderComments": "string",
                  "senderComments_acc": "integer",
                  "additionalNotes": "string"
                },
                "relevantTests": "string",
                "relevantTests_acc": "integer",
                "receiptDate": "string",
                "receiptDate_acc": "integer",
                "mostRecentReceiptDate": "string",
                "mostRecentReceiptDate_acc": "integer",
                "reporters": [
                  {
                    "country": "string",
                    "country_acc": "integer",
                    "country_viewid": "string",
                    "city": "string",
                    "city_acc": "integer",
                    "givenName": "string",
                    "givenName_acc": "integer",
                    "firstName": "string",
                    "firstName_acc": "integer",
                    "middleName": "string",
                    "middleName_acc": "integer",
                    "lastName": "string",
                    "lastName_acc": "integer",
                    "postcode": "string",
                    "postcode_acc": "integer",
                    "title": "string",
                    "title_acc": "integer",
                    "street": "string",
                    "street_acc": "integer",
                    "organization": "string",
                    "organization_acc": "integer",
                    "state": "string",
                    "state_acc": "integer",
                    "department": "string",
                    "department_acc": "integer",
                    "email": "string",
                    "email_acc": "integer",
                    "primary": "boolean",
                    "fax": "string",
                    "telephone": "string",
                    "qualification": "string",
                    "qualification_acc": "integer",
                    "qualification_viewid": "string"
                  }
                ],
                "study": {
                  "additionalNotes": "string",
                  "caseBlindingStatus": "string",
                  "studyNumber": "string",
                  "studyNumber_viewid": "string",
                  "studyNumber_acc": "integer",
                  "studyType": "string",
                  "studyType_viewid": "string",
                  "studyType_acc": "integer",
                  "studyArm": "string"
                },
                "sourceType": [
                  {
                    "additionalNotes": "string",
                    "value": "string",
                    "value_viewid": "string",
                    "value_acc": "integer"
                  }
                ],
                "patient": {
                  "additionalNotes": "string",
                  "additionalRelevantMedicalHistory": "string",
                  "gender": "string",
                  "gender_acc": "integer",
                  "gender_viewid": "string",
                  "patientId": "string",
                  "patientId_acc": "integer",
                  "weight": "string",
                  "weight_acc": "integer",
                  "weight_viewid": "string",
                  "weightUnit": "string",
                  "weightUnit_acc": "integer",
                  "height": "string",
                  "height_viewid": "string",
                  "height_acc": "integer",
                  "heightUnit": "string",
                  "heightUnit_acc": "integer",
                  "pregnancy": "string",
                  "pregnancy_viewid": "string",
                  "patientDob": "string",
                  "patientDob_acc": "integer",
                  "name": "string",
                  "name_acc": "integer",
                  "age": {
                    "inputValue": "string",
                    "inputValue_viewid": "string",
                    "inputValue_acc": "integer",
                    "ageType": "string"
                  },
                  "medicalHistories": [
                    {
                      "additionalNotes": "string",
                      "startDate": "string",
                      "endDate": "string",
                      "historyConditionType": "string",
                      "reportedReaction": "string",
                      "reactionCoded": "string",
                      "historyNote": "string",
                      "continuing": "string"
                    }
                  ],
                  "pastDrugHistories": [
                    {
                      "additionalNotes": "string",
                      "startDate": "string",
                      "endDate": "string",
                      "historyConditionType": "string",
                      "drugName": "string",
                      "drugNameCoded": "string",
                      "historyNote": "string",
                      "continuing": "string",
                      "nameParts": [
                        {
                          "seqNum": "integer",
                          "deleted": "string",
                          "internalRowId": "string",
                          "partType": "string",
                          "partName": "string",
                          "customProperty": "string"
                        }
                      ],
                      "substances": [
                        {
                          "seqNum": "integer",
                          "deleted": "string",
                          "internalRowId": "string",
                          "name": "string",
                          "strength": "string",
                          "customProperty": "string"
                        }
                      ],
                      "drugIndication": [
                        {
                          "reportedReaction": "string",
                          "reactionCoded": "string"
                        }
                      ],
                      "drugReaction": [
                        {
                          "reportedReaction": "string",
                          "reactionCoded": "string"
                        }
                      ]
                    }
                  ]
                },
                "parent": {
                  "additionalNotes": "string",
                  "additionalRelevantMedicalHistory": "string",
                  "gender": "string",
                  "gender_acc": "integer",
                  "gender_viewid": "string",
                  "patientId": "string",
                  "patientId_acc": "integer",
                  "weight": "string",
                  "weight_acc": "integer",
                  "weight_viewid": "string",
                  "weightUnit": "string",
                  "weightUnit_acc": "integer",
                  "height": "string",
                  "height_viewid": "string",
                  "height_acc": "integer",
                  "heightUnit": "string",
                  "heightUnit_acc": "integer",
                  "pregnancy": "string",
                  "pregnancy_viewid": "string",
                  "patientDob": "string",
                  "patientDob_acc": "integer",
                  "name": "string",
                  "name_acc": "integer",
                  "age": {
                    "inputValue": "string",
                    "inputValue_viewid": "string",
                    "inputValue_acc": "integer",
                    "ageType": "string"
                  },
                  "medicalHistories": [
                    {
                      "additionalNotes": "string",
                      "startDate": "string",
                      "endDate": "string",
                      "historyConditionType": "string",
                      "reportedReaction": "string",
                      "reactionCoded": "string",
                      "historyNote": "string",
                      "continuing": "string"
                    }
                  ],
                  "pastDrugHistories": [
                    {
                      "additionalNotes": "string",
                      "startDate": "string",
                      "endDate": "string",
                      "historyConditionType": "string",
                      "drugName": "string",
                      "drugNameCoded": "string",
                      "historyNote": "string",
                      "continuing": "string",
                      "nameParts": [
                        {
                          "seqNum": "integer",
                          "deleted": "string",
                          "internalRowId": "string",
                          "partType": "string",
                          "partName": "string",
                          "customProperty": "string"
                        }
                      ],
                      "substances": [
                        {
                          "seqNum": "integer",
                          "deleted": "string",
                          "internalRowId": "string",
                          "name": "string",
                          "strength": "string",
                          "customProperty": "string"
                        }
                      ],
                      "drugIndication": [
                        {
                          "reportedReaction": "string",
                          "reactionCoded": "string"
                        }
                      ],
                      "drugReaction": [
                        {
                          "reportedReaction": "string",
                          "reactionCoded": "string"
                        }
                      ]
                    }
                  ]
                },
                "events": [
                  {
                    "seq_num": "integer",
                    "additionalNotes": "string",
                    "country": "string",
                    "country_acc": "integer",
                    "country_viewid": "string",
                    "reactionCoded": "string",
                    "reactionCoded_viewid": "string",
                    "reactionCoded_acc": "integer",
                    "reportedReaction": "string",
                    "reportedReaction_viewid": "string",
                    "reportedReaction_acc": "integer",
                    "medicallyConfirmed": "string",
                    "medicallyConfirmed_acc": "integer",
                    "startDate": "string",
                    "startDate_acc": "integer",
                    "endDate": "string",
                    "endDate_acc": "integer",
                    "seriousnesses": [
                      {
                        "value": "string",
                        "value_viewid": "string",
                        "value_acc": "integer"
                      }
                    ],
                    "outcome": "string",
                    "outcome_viewid": "string",
                    "outcome_acc": "integer"
                  }
                ],
                "products": [
                  {
                    "seq_num": "string",
                    "additionalNotes": "string",
                    "studyProduct": "string",
                    "license_value": "string",
                    "license_value_viewid": "string",
                    "license_value_acc": "integer",
                    "nameParts": [
                      {
                        "seqNum": "integer",
                        "deleted": "string",
                        "internalRowId": "string",
                        "partType": "string",
                        "partName": "string",
                        "customProperty": "string"
                      }
                    ],
                    "ingredients": [
                      {
                        "value": "string",
                        "value_acc": "integer",
                        "value_viewid": "string",
                        "strength": "string",
                        "strength_acc": "integer",
                        "unit": "string",
                        "unit_acc": "integer"
                      }
                    ],
                    "product_type": [
                      {
                        "value": "string",
                        "value_acc": "integer"
                      }
                    ],
                    "dosageForm_value": "string",
                    "dosageForm_value_acc": "integer",
                    "dosageForm_value_viewid": "string",
                    "concentration": [
                      {
                        "value": "string",
                        "value_acc": "integer",
                        "unit": "string",
                        "unit_acc": "integer"
                      }
                    ],
                    "indications": [
                      {
                        "reportedReaction": "string",
                        "reportedReaction_acc": "integer",
                        "reactionCoded": "string",
                        "reactionCoded_acc": "integer"
                      }
                    ],
                    "devices": [
                      {
                        "malfunctions": "string"
                      }
                    ],
                    "role_value": "string",
                    "doseInformations": [
                      {
                        "dose_inputValue": "string",
                        "dose_inputValue_acc": "integer",
                        "dose_inputValue_viewid": "string",
                        "frequency_value": "string",
                        "frequency_value_acc": "integer",
                        "frequency_value_viewid": "string",
                        "duration": "string",
                        "duration_acc": "integer",
                        "route_value": "string",
                        "route_value_viewid": "string",
                        "route_value_acc": "integer",
                        "customProperty_batchNumber_value": "string",
                        "customProperty_batchNumber_value_acc": "integer",
                        "customProperty_expiryDate": "string",
                        "customProperty_expiryDate_acc": "integer",
                        "endDate_acc": "integer",
                        "endDate": "string",
                        "startDate": "string",
                        "startDate_acc": "integer",
                        "description": "string",
                        "description_acc": "integer"
                      }
                    ],
                    "actionTaken": {
                      "value": "string",
                      "value_acc": "integer"
                    },
                    "additionalDrugInfo": [
                      {
                        "value": "string"
                      }
                    ],
                    "implantDate": "string",
                    "explantDate": "string",
                    "blinded": "string"
                  }
                ],
                "productEventMatrix": [
                  {
                    "product_seq_num": "string",
                    "product_seq_num_acc": "integer",
                    "event_seq_num": "string",
                    "event_seq_num_acc": "integer",
                    "rechallenge": {
                      "value": "string",
                      "value_acc": "integer"
                    },
                    "dechallenge": {
                      "value": "string",
                      "value_acc": "integer"
                    },
                    "relatednessAssessments": [
                      {
                        "result": {
                          "value": "string",
                          "value_acc": "integer"
                        }
                      }
                    ]
                  }
                ],
                "seriousnesses": [
                  {
                    "value": "string",
                    "value_acc": "integer"
                  }
                ],
                "deathDetail": {
                  "additionalNotes": "string",
                  "deathDate": {
                    "date": "string",
                    "date_acc": "integer"
                  },
                  "autopsyDone": "string",
                  "autopsyDone_acc": "integer",
                  "deathCauses": [
                    {
                      "causeType": "string",
                      "reportedReaction": "string",
                      "reportedReaction_acc": "integer"
                    }
                  ]
                },
                "tests": [
                  {
                    "seq_num": "integer",
                    "startDate": "string",
                    "testName": "string",
                    "testName_acc": "integer",
                    "testName_viewid": "string",
                    "testAssessment": "string",
                    "testNotes": "string",
                    "testResult": "string",
                    "testResultUnit": "string",
                    "testHigh": "string",
                    "testLow": "string"
                  }
                ],
                "literatures": {
                  "author": "string",
                  "title": "string",
                  "journal": "string",
                  "vol": "string",
                  "year": "string",
                  "pages": "string"
                },
                "entitylocation": {
                  "entities": {
                    "0": {
                      "entity_label": "string",
                      "entity_text": "string",
                      "sent_id": "string",
                      "start": "string",
                      "end": "string"
                    }
                  },
                  "sentences": {
                    "0": "string",
                    "1": "string"
                  }
                },
                "pregnancyInformation": {
                  "noOfFetus": "string",
                  "gravida": "string",
                  "para": "string",
                  "retrospectiveProspective": "string",
                  "gestatioPeriod": "string",
                  "dueDate": "string",
                  "neonates": [
                    {
                      "pregnancyOutcome": "string",
                      "fetalOutcome": "string",
                      "deliveryType": "string",
                      "birthWeight": "string",
                      "gestAgeAtPregOutcome": "string",
                      "pregnancyNotes": "string",
                      "pregnancyOutcomeDate": "string",
                      "apgarScores": [
                        {
                          "type": {
                            "value": "string",
                            "value_acc": "integer"
                          },
                          "score": "string"
                        }
                      ]
                    }
                  ]
                },
                "module_name": "string",
                "model_type": "string",
                "code": "integer",
                "message": "string"
              }
            ```
      - **`500`:** 
        - Description: Internal server error
          - Content Type: `application/json`
            - Schema:
              ```json 
                {
                  "message":  "string"
                }
              ```    
      
        
            
                      