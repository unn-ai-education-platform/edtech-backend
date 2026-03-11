# user-stories.md

## Student

1. **Submit text assignment**
   - As a student, I can submit my text for review against a rubric.

2. **Check review status**
   - As a student, I can see whether my submission is still under review or the final result is ready.
   - The status must be generic and must not mention AI.

3. **View final result**
   - As a student, I can view the final teacher-approved result once it is ready.

## Teacher

1. **Create and manage rubrics**
   - As a teacher, I can create a rubric with criteria and weights.
   - In MVP, created rubrics are immutable.

2. **View submissions**
   - As a teacher, I can list submissions and open submission details.

3. **Run evaluation**
   - As a teacher, I can trigger evaluation for a submission, creating an async job.

4. **Review AI draft**
   - As a teacher, I can view the AI draft evaluation (teacher-only).

5. **Finalize result**
   - As a teacher, I can accept the AI draft or override it and create the final decision.
   - Creating the final decision makes it visible to the student immediately in MVP.
