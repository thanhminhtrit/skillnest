# Security Configuration Guide

## Environment Variables

This application requires the following environment variables to be set for security reasons. Sensitive credentials are no longer hardcoded in the configuration files.

### Required Environment Variables

#### Database Configuration
- `DATABASE_URL` - PostgreSQL database connection URL
- `DATABASE_USERNAME` - Database username
- `DATABASE_PASSWORD` - Database password (REQUIRED - no default)

#### JWT Configuration
- `JWT_SECRET` - Secret key for JWT token signing (REQUIRED - no default, minimum 256 bits)
- `JWT_EXPIRATION` - JWT token expiration time in milliseconds (default: 86400000 = 24 hours)

#### Server Configuration
- `SERVER_PORT` - Application server port (default: 8080)

### Setting Up Environment Variables

#### For Local Development

1. Copy the `.env.example` file to `.env`:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` and fill in the actual values:
   ```bash
   DATABASE_PASSWORD=your_actual_password
   JWT_SECRET=your_actual_jwt_secret_key_minimum_256_bits
   ```

3. Load environment variables (depending on your setup):
   - Using `direnv`: Place variables in `.envrc`
   - Using IDE: Configure environment variables in run configuration
   - Using terminal: Export variables before running
     ```bash
     export DATABASE_PASSWORD=your_password
     export JWT_SECRET=your_secret
     ./mvnw spring-boot:run
     ```

#### For Production Deployment

Set environment variables in your deployment platform:

- **Docker**: Use `-e` flags or `docker-compose.yml` environment section
- **Kubernetes**: Use ConfigMaps and Secrets
- **Cloud Platforms** (Azure, AWS, GCP): Use their environment variable/secrets management services
- **Heroku**: Use `heroku config:set KEY=VALUE`

### Security Best Practices

1. **Never commit** `.env` files to version control (already in `.gitignore`)
2. **Rotate secrets regularly**, especially JWT secret keys
3. **Use strong passwords** for database access
4. **Generate JWT secret** using a cryptographically secure random generator:
   ```bash
   # Generate a secure JWT secret (256+ bits)
   openssl rand -base64 64
   ```
5. **Restrict database access** by IP/network when possible
6. **Use HTTPS** in production
7. **Enable database SSL** for production connections

### Migration Notes

**Previous versions** of this application had hardcoded credentials in `application.yml` files. These have been removed for security. If you're upgrading:

1. Extract the old hardcoded values
2. Set them as environment variables
3. Test the application starts correctly
4. Rotate all credentials (especially if the old code was committed to a public repository)

### Troubleshooting

If the application fails to start with errors about missing properties:

1. Verify all required environment variables are set:
   ```bash
   echo $DATABASE_PASSWORD
   echo $JWT_SECRET
   ```

2. Check for typos in variable names

3. Ensure no extra spaces around `=` in `.env` files

4. Verify the application can read the environment variables (check logs)
