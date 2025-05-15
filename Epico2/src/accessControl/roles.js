// Define the permissions assigned to each user role
const rolesPermissions = {
    admin: [
        'create:password',     // Can create passwords for apps
        'update:password',     // Can update any password
        'read:password',       // Can view any password
        'read:apps',           // Can view list of all apps
        'update:apps'          // Can update app info
    ],
    client: [
        'create:password',        // Can create passwords
        'read:own:password',      // Can read only their own password
        'update:own:password'     // Can update only their own password
    ],
    public: [] // No permissions for unauthenticated or guest users
};

// Export the role-permission mapping
module.exports = rolesPermissions;
