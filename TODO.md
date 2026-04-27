# Performance Fixes - TODO

## Backend Fixes
- [x] 1. Add DB indexes to Task, Note, ChatHistory entities
- [x] 2. Add count queries to repositories (avoid loading full entities)
- [x] 3. Replace in-memory aggregation in UserService with DB queries
- [x] 4. Replace in-memory aggregation in TaskService with DB queries
- [x] 5. Add pagination support to Task, Note, ChatHistory repositories
- [x] 6. Add @Async to OpenAIService.chat()
- [x] 7. Add Spring Cache for stats and activity
- [x] 8. Tune HikariCP connection pool
- [x] 9. Add bulk delete for chat history clear
- [x] 10. Commit all changes to git

## Frontend Fixes
- [x] 11. Add Axios timeout in api.js
- [x] 12. Add useDebounce hook for search inputs
- [x] 13. Commit frontend changes to git

