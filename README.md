Jasoner
================================
Convert json data to another format using musctache syntax.

**Routes API Rest**

#POST    /jasoner_token                     
    Create new token to use other API call. Don't loose you token. Also token is not secure, it it just UUID and may be brute forced
    any time.

#GET     /jasoner_template/:token           
    Return all jasoner mustache template created by token.
#POST    /jasoner_template/:token           
    Create new Jasoner template. Request body is mustache template and response contains template id.
#GET     /jasoner_template/:token/:id
    Get jasoner template content       
#PUT     /jasoner_template/:token/:id
    Update jasoner template. Request body is mustache template.
#DELETE  /jasoner_template/:token/:id
    Delete jasoner template

#POST    /jasoner/:token/:id
    Convert json to another format. Request body is source json data and response is converted data based to mustache template                
#POST    /jasoner/:token/:id/:encUrl        
    Conver json to another format and forward converted data to the encoded url. 
    Request body is source json data and post mustached json data forward to the encoded url. 
    
