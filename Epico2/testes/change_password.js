
//testes para o endpoint de mudanca de password
//

/*{
  "appid": "appteste",
  "name": "APPteste",
  "secret": "test123",
  "password": "test123"
  "role": "admin"
} */

/*
    Token: (tem limite de tempo)

    eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBpZCI6ImFwcHRlc3RlIiwicm9sZSI6ImNsaWVudCIsImlhdCI6MTc0NzkxMDY3NSwiZXhwIjoxNzQ3OTE0Mjc1fQ.eCsxGcqQ2OdGe9-Mma9zH5aI7b4W2oW5RSnvi2iUO3A

*/


//teste de sucesso

// Verificar status code
pm.test("Status code é 200 (sucesso)", function() {
    pm.response.to.have.status(201);
});

// Verificar mensagem de sucesso
pm.test("Mensagem de sucesso recebida", function() {
    var jsonData = pm.response.json();
    pm.expect(jsonData.message).to.eql("Password created successfully!");
});

//password duplicada
pm.test("Teste de conflito Status code 409", function() {
    pm.response.to.have.status(409);
});

//password duplicada
pm.test("Mensagem de password já existente", function() {
    var jsonData = pm.response.json();
    pm.expect(jsonData.error).to.eql("Password for this app already exists.");
});


//teste sem token
pm.test("Sem token", () => {
    if(pm.response.code === 403 && pm.response.json().error.includes("No token provided")) {
        pm.expect(pm.response.code).to.equal(403);
    }
});

// Teste para token inválido
pm.test("Token Inválido", () => {
    if(pm.response.code === 403 && pm.response.json().error.includes("Invalid token")) {
        pm.expect(pm.response.code).to.equal(403);
    }
});

// Teste para permissões insuficientes
pm.test("Deve verificar permissões adequadamente", () => {
    if(pm.response.code === 403 && pm.response.json().error.includes("Not enough privileges")) {
        pm.expect(pm.response.code).to.equal(403);
    }
});