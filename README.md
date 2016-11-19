Jasoner
================================
Convert json data to another format using musctache syntax.

# Routes API Rest
GET     /jasoner_template           
Get List of jasoner templates

POST    /jasoner_template           
    Create new Jasoner template. Request body is mustache template and response contains id for next request

GET     /jasoner_template/:id       
    Get jasoner template content

PUT     /jasoner_template/:id       
    Update jasoner template. Request body is mustache template.

DELETE  /jasoner_template/:id       
    Delete jasoner template


POST    /jasoner/:id
   request body is source json data and response is converted data based to mustache template
   
POST    /jasoner/:id/:encoded_url
   request body is source json data and post mustached json data forward to the encoded url. 
   Response is converted data based to mustache template.


