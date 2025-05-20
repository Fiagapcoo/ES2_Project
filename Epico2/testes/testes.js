
//testes para o endpoint health (Metodo GET)

//teste para verificar todos os metodos de reuqisicao
pm.test("Verifica método HTTP da requisição", function () {

    const metodo = pm.request.method;
    console.log("metodo usado:",metodo);
    switch(metodo){
    case "GET":
            pm.response.to.have.status(200);
            break;
    case "POST":
            pm.response.to.have.status(404);
            break;
    case "PUT":
            pm.response.to.have.status(404);   
            break;
    case "DELETE":
            pm.response.to.have.status(404);
            break;
     }
});


//teste para verificar a estrutura esperada para todos os metodos
pm.test("Estrutura correta por método HTTP", function () {
    const metodo = pm.request.method;

    switch (metodo) {
        case "GET":
            const jsonData = pm.response.json();

            pm.expect(pm.response.code).to.eql(200);
            pm.expect(jsonData).to.have.property("status", "UP");
            pm.expect(jsonData).to.have.property("timestamp");

            const timestamp = jsonData.timestamp;
            const isoRegex = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/;
            pm.expect(timestamp).to.match(isoRegex);
            break;

        case "POST":
        case "PUT":
        case "DELETE":
            const html = pm.response.text();

            // Teste para resposta de erro HTML padrão do Express
            pm.expect(pm.response.code).to.be.oneOf([404, 405]);
            pm.expect(html).to.include("<!DOCTYPE html>");
            pm.expect(html).to.include("<title>Error</title>");
            pm.expect(html).to.include(`<pre>Cannot ${metodo} /health</pre>`);
            break;

        default:
            pm.expect.fail(`Método HTTP '${metodo}' não tratado no script`);
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

pm.test("Validação do endpoint /register para vários métodos", function () {
    const metodo = pm.request.method;

    switch (metodo) {
        case "POST":
            if (pm.response.code === 201) {
                const data = pm.response.json();
                pm.expect(data).to.have.property("message", "App registered with success!");
                pm.expect(data).to.have.property("token");
                pm.expect(data.token).to.match(/^[\w-]+\.[\w-]+\.[\w-]+$/); // Regex básico JWT
            } else if (pm.response.code === 400) {
                const data = pm.response.json();
                pm.expect(data).to.have.property("error", "All fields are mandatory.");
            } else if (pm.response.code === 409) {
                const data = pm.response.json();
                pm.expect(data).to.have.property("error", "App already registered.");
            } else if (pm.response.code === 500) {
                const data = pm.response.json();
                pm.expect(data).to.have.property("error", "Internal server error during registration.");
            } else {
                pm.expect.fail("Status inesperado para POST: " + pm.response.code);
            }
            break;

        case "GET":
        case "PUT":
        case "DELETE":
        case "PATCH":
            // Espera erro 404 ou 405 para métodos não suportados
            pm.expect(pm.response.code).to.be.oneOf([404, 405]);
            const html = pm.response.text();
            pm.expect(html).to.include("<!DOCTYPE html>");
            pm.expect(html).to.include("<title>Error</title>");
            pm.expect(html).to.include(`<pre>Cannot ${metodo} /register</pre>`);
            break;

        default:
            pm.expect.fail(`Método HTTP '${metodo}' não tratado no script`);
    }
});


pm.test("Validar resposta do POST /api/app", () => {
  const status = pm.response.code;
  const jsonData = pm.response.json();

  if (status === 201) {
    const successSchema = {
      "type": "object",
      "properties": {
        "message": { "type": "string", "const": "App registered with success!" },
        "token": { "type": "string", "pattern": "^[\\w-]+\\.[\\w-]+\\.[\\w-]+$" }
      },
      "required": ["message", "token"],
      "additionalProperties": false
    };
    pm.expect(pm.response).to.have.jsonSchema(successSchema);

  } else if (status === 400) {
    const badRequestSchema = {
      "type": "object",
      "properties": {
        "error": { "type": "string", "const": "All fields are mandatory." }
      },
      "required": ["error"],
      "additionalProperties": false
    };
    pm.expect(pm.response).to.have.jsonSchema(badRequestSchema);

  } else if (status === 409) {
    const conflictSchema = {
      "type": "object",
      "properties": {
        "error": { "type": "string", "const": "App already registered." }
      },
      "required": ["error"],
      "additionalProperties": false
    };
    pm.expect(pm.response).to.have.jsonSchema(conflictSchema);

  } else if (status === 500) {
    const internalErrorSchema = {
      "type": "object",
      "properties": {
        "error": { "type": "string", "const": "Internal server error during registration." }
      },
      "required": ["error"],
      "additionalProperties": false
    };
    pm.expect(pm.response).to.have.jsonSchema(internalErrorSchema);

  } else {
    pm.expect.fail(`Status code inesperado: ${status}`);
  }
});
