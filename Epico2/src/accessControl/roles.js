const rolesPermissions = {
    admin: [
        'create:password', 
        'update:password', 
        'read:password', 
        'read:apps',
        'update:apps'
    ],
    client: [
        'create:password', 
        'read:own:password',
        'update:own:password'
    ],
    public: []
};

module.exports = rolesPermissions;