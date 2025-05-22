
//testes para o endpoint health (Metodo GET)

//teste para verificar todos os metodos de reuqisicao

  pm.test("Validar resposta do GET /health", () => {
    const status = pm.response.code;
    const jsonData = pm.response.json();

    if (status === 200) {
      const successSchema = {
        "type": "object",
        "properties": {
          "status": { 
            "type": "string", 
            "const": "UP",
            "description": "Deve indicar que o serviço está operacional"
          },
          "timestamp": {
            "type": "string",
            "format": "date-time",
            "description": "Deve ser uma data ISO 8601 válida"
          }
        },
        "required": ["status", "timestamp"],
        "additionalProperties": false
      };
      
      // Valida o schema da resposta
      pm.expect(pm.response).to.have.jsonSchema(successSchema);
      
      // Validações adicionais
      const now = new Date();
      const serverTime = new Date(jsonData.timestamp);
      const timeDiff = Math.abs(now - serverTime);
      
      pm.expect(timeDiff).to.be.lessThan(
        10000, 
        "O timestamp deve ter no máximo 10 segundos de diferença do tempo atual"
      );

    } else if (status === 503) {
      const serviceUnavailableSchema = {
        "type": "object",
        "properties": {
          "status": { 
            "type": "string", 
            "const": "DOWN",
            "description": "Deve indicar que o serviço está inoperante"
          },
          "timestamp": {
            "type": "string",
            "format": "date-time",
            "description": "Deve ser uma data ISO 8601 válida"
          },
          "details": {
            "type": "object",
            "description": "Informações adicionais sobre o problema"
          }
        },
        "required": ["status", "timestamp"],
        "additionalProperties": true
      };
      pm.expect(pm.response).to.have.jsonSchema(serviceUnavailableSchema);

    } else {
      pm.expect.fail(`Status code inesperado: ${status}`);
    }
  });





//testes para o endpoit /api/app (Metodo POST)

//body usado
/*
{
  "appid": "myapp123",
  "name": "Minha Aplicação",
  "secret": "segredoSuperSecreto",
  "role": "admin",
  "password": "senha123"
}  */

//testes no caso de sucesso---------------

pm.test("Status code is 201", function() {
    pm.response.to.have.status(201);
});

pm.test("Resposta tem mensagem de sucesso", function() {
    var jsonData = pm.response.json();
    pm.expect(jsonData.message).to.eql("App registered with success!");
});

pm.test("Resposta tem o JWT token", function() {
    var jsonData = pm.response.json();
    pm.expect(jsonData.token).to.be.a('string');
});

pm.test("Content-Type is application/json", function() {
    pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});

//--------------------------------------------------------------------------

//teste app repetida

/*{
  "appid": "myapp123",
  "name": "Minha Aplicação",
  "secret": "segredoSuperSecreto",
  "role": "admin",
  "password": "senha123"
}*/

pm.test("Status code é 409 Conflicto", function() {
    pm.response.to.have.status(409);
});

// Verificar mensagem de erro
pm.test("Mensagem de erro", function() {
    var jsonData = pm.response.json();
    pm.expect(jsonData.error).to.eql("App already registered.");
});

// Verificar content type
pm.test("Content-Type is application/json", function() {
    pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});

//teste de falta de campos obrigatorios ou campos nulos

/*{
  "appid": null,
  "name": "Test App",
  "secret": "test",
  "password": "test"
}*/

/*{
  "appid": "",
  "name": "Test App",
  "secret": "test",
  "password": "test"
}*/

/*{
  "name": "Test Application",
  "secret": "test_secret",
  "password": "test_password",
  "role": "client"
}*/

// Verificar status code de bad request
pm.test("Status code is 400 for missing field", function() {
    pm.response.to.have.status(400);
});

// Verificar mensagem de erro
pm.test("Response has mandatory fields error message", function() {
    var jsonData = pm.response.json();
    pm.expect(jsonData.error).to.eql("All fields are mandatory.");
});

// Verificar content type
pm.test("Content-Type is application/json", function() {
    pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});

//Teste para o caso de criar sem role

/*{
  "appid": "minhaapp12345",
  "name": "Appsemrole",
  "secret": "segredoSuperSecreto",
  "role": "admin",
  "password": "senha123"
}*/

pm.test("Status code is 201 when only role is missing", function() {
    pm.response.to.have.status(201);
});

//teste para caracteres especiais
/*
{
  "appid": "app_!@#$%^&*()",
  "name": "Special Chars App",
  "secret": "test123",
  "password": "test123"
}
*/

pm.test("Status code deve ser 201 com caracteres especiais", function() {
    pm.response.to.have.status(201);
});