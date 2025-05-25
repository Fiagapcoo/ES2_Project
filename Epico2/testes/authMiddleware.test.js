// testes/authMiddleware.test.js
const jwt = require("jsonwebtoken");
const { authenticateJWT, authorize } = require("../src/middlewares/auth");
const rolesPermissions = require("../src/accessControl/roles");

const JWT_SECRET = process.env.JWT_SECRET || "testsecret";

describe("Middleware de autenticação", () => {
  it("Deve bloquear requisição sem token", () => {
    // Testa: if (!token) [V]
    const req = { headers: {} };
    const res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn(),
    };
    const next = jest.fn();

    authenticateJWT(req, res, next);

    expect(res.status).toHaveBeenCalledWith(403);
    expect(res.json).toHaveBeenCalledWith({
      error: "Access denied. No token provided.",
    });
    expect(next).not.toHaveBeenCalled();
  });

  it("Deve permitir requisição com token válido", () => {
    // Testa: if (!err) e token válido → passa pelo next() [V]
    const token = jwt.sign({ appid: "123", role: "client" }, JWT_SECRET, {
      expiresIn: "1h",
    });

    const req = {
      headers: { authorization: `Bearer ${token}` },
    };

    const res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn(),
    };

    const next = jest.fn(() => {
    try {
      expect(req.user).toBeDefined();
      expect(req.user.appid).toBe("123");
      expect(next).toHaveBeenCalled();
      done();
    } catch (err) {
      done(err);
    }
  });

  authenticateJWT(req, res, next);
  });
});

describe("Middleware de autorização", () => {
  it("Deve bloquear se role não tiver permissão", () => {
    // Testa: if (!allowedPermissions.includes(permission)) [V]
    const req = {
      user: { appid: "123", role: "public" },
      params: { appid: "123" },
    };
    const res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn(),
    };
    const next = jest.fn();

    const middleware = authorize("read:password");
    middleware(req, res, next);

    expect(res.status).toHaveBeenCalledWith(403);
    expect(res.json).toHaveBeenCalledWith({
      error: "Access denied. Not enough privileges.",
    });
    expect(next).not.toHaveBeenCalled();
  });

  it("Deve permitir acesso se role tiver permissão e for dono", () => {
    // Testa:
    // if (permission.endsWith('own:password')) [V]
    // if (userAppid !== targetAppid) [F]
    // if (!allowedPermissions.includes(permission)) [F]
    const req = {
      user: { appid: "abc", role: "client" },
      params: { appid: "abc" },
    };
    const res = {};
    const next = jest.fn();

    const middleware = authorize("read:own:password");
    middleware(req, res, next);

    expect(next).toHaveBeenCalled();
  });
});
