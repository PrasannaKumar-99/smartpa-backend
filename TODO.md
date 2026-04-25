# Port Binding Fix for Render Deployment - TODO

## Steps
- [x] 1. Identify root cause: Dockerfile doesn't activate `prod` Spring profile
- [x] 2. Identify invalid `EXPOSE $PORT` in Dockerfile (runtime env var not expanded)
- [x] 3. Get user confirmation for the plan
- [x] 4. Fix Dockerfile - remove invalid EXPOSE, add prod profile activation
- [ ] 5. Rebuild and redeploy to Render

