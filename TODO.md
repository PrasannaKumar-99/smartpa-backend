# Performance Fixes - TODO

## Backend Fixes
- [ ] 1. Add DB indexes to Task, Note, ChatHistory entities
- [ ] 2. Add count queries to repositories (avoid loading full entities)
- [ ] 3. Replace in-memory aggregation in UserService with DB queries
- [ ] 4. Replace in-memory aggregation in TaskService with DB queries
- [ ] 5. Add pagination to Task, Note, ChatHistory endpoints
- [ ] 6. Add @Async to OpenAIService.chat()
- [ ] 7. Add Spring Cache for stats and activity
- [ ] 8. Tune HikariCP connection pool
- [ ] 9. Add bulk delete for chat history clear
- [ ] 10. Commit all changes to git

## Frontend Fixes
- [ ] 11. Add Axios timeout in api.js
- [ ] 12. Add debounce to NotesPage search
- [ ] 13. Commit frontend changes to git

