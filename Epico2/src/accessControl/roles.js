const rolesPermissions = {
    admin: ['create:password', 'update:password', 'read:password', 'read:apps'],
    client: ['create:password', 'read:password'],
};
  
module.exports = rolesPermissions;
  