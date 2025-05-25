// testes/appController.test.js
const request = require("supertest");
const jwt = require("jsonwebtoken");
const app = require("../index");
const { apps } = require("../src/models/app");

const JWT_SECRET = process.env.JWT_SECRET;

jest.mock('../src/services/logging', () => ({
  logAccess: jest.fn()
}));

beforeEach(() => {
  apps.length = 0;
});

describe("POST /api/app", () => {
  it("Deve retornar 400 se faltar algum campo obrigatório", async () => {
    // Testa: if (!appid || !name || !secret || !password) [V]
    const res = await request(app)
      .post("/api/app")
      .send({ name: "App Teste", secret: "123" });

    expect(res.status).toBe(400);
    expect(res.body.error).toMatch(/mandatory/i);
  });
});

describe("POST /api/app/password/:appid", () => {
  it("Deve retornar 400 se a senha não for fornecida", async () => {
    // Testa: if (!password) [V]
    const token = jwt.sign({ appid: "1234", role: "client" }, JWT_SECRET, {
      expiresIn: "1h",
    });

    apps.push({
      appid: "1234",
      name: "Teste",
      secret: "hash",
      roles: ["client"],
      password: "123",
    });

    const res = await request(app)
      .post("/api/app/password/1234")
      .set("Authorization", `Bearer ${token}`)
      .send({});

    expect(res.status).toBe(400);
  });
});

describe("PUT /api/app/password/:appid", () => {
  it("Deve retornar 400 se a nova senha estiver vazia", async () => {
    // Testa: if (!password) [V]
    const token = jwt.sign({ appid: "1234", role: "client" }, JWT_SECRET, {
      expiresIn: "1h",
    });

    apps.push({
      appid: "1234",
      name: "Teste",
      secret: "hash",
      roles: ["client"],
      password: "abc",
    });

    const res = await request(app)
      .put("/api/app/password/1234")
      .set("Authorization", `Bearer ${token}`)
      .send({});

    expect(res.status).toBe(400);
  });
});
describe("GET /api/app/password/:appid", () => {
  it("Deve retornar 404 se o app não existir", async () => {
    // Testa: if (!appExists) [V]
    const token = jwt.sign({ appid: "9999", role: "client" }, JWT_SECRET, {
      expiresIn: "1h",
    });

    const res = await request(app)
      .get("/api/app/password/9999")
      .set("Authorization", `Bearer ${token}`);

    expect(res.status).toBe(404);
  });
});

describe("GET /api/apps", () => {
  it("Deve retornar 200 com a lista de apps", async () => {
    // Testa: retorno da lista de apps autenticado e autorizado [V]
    const token = jwt.sign({ appid: "adminApp", role: "admin" }, JWT_SECRET, {
      expiresIn: "1h",
    });

    apps.push({
      appid: "adminApp",
      name: "Admin",
      secret: "hash",
      roles: ["admin"],
      password: "abc",
    });

    const res = await request(app)
      .get("/api/apps")
      .set("Authorization", `Bearer ${token}`);

    expect(res.status).toBe(200);
    expect(Array.isArray(res.body.apps)).toBe(true);
  });
});

describe('POST /api/import/apps', () => {
  it('Deve importar apps simuladas da API externa (stub)', async () => {
    // Testa: 
    // importação de apps com token válido [V]
    // if (importedApps.length > 0) [V]

     const token = jwt.sign({ appid: 'adminApp', role: 'admin' }, process.env.JWT_SECRET, { expiresIn: '1h' });

    const res = await request(app)
      .post('/api/import/apps')
      .set('Authorization', `Bearer ${token}`);

    expect(res.status).toBe(200);
    expect(res.body.imported).toBeGreaterThan(0);
    expect(apps.length).toBeGreaterThan(0);
  });
});
