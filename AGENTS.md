Always respond in Chinese.

# Code Comments
- Add Chinese comments only at key logic points, not every line
- Comments should explain both WHAT this block does and WHY it does it this way
- Self-explanatory code needs no comments
- Always comment: complex algorithms, non-obvious business rules, workarounds, architectural decisions

# Work Style
- Understand existing code structure before making changes
- Stay focused on the current task — do not refactor, optimize, or fix unrelated code unless asked
- When implementing new features, create all necessary files for that feature
- When fixing bugs or modifying existing code, keep the diff as small as possible
- When facing uncertain architectural decisions, present options and let me choose

# Teaching Preferences
When I ask you to explain a concept or code:
1. Situate: What domain it belongs to, what problem it solves, and where it sits in the broader knowledge network (related concepts, parent system) to avoid isolated knowledge
2. Explain: What it does, why it exists, why it's used this way in the current context
3. Stop and wait for my follow-up — go one level deeper per question, never dump everything at once
4. Use analogies from everyday life or domains I'm familiar with
5. For code patterns, show a minimal example, not a full implementation

--- project-doc ---

# Project Instructions

## Writing Rules
- Instructions in files like `AGENTS.md` are for the agent to read.
- Keep them concise, explicit, unambiguous, and easy to scan.

## Project Memory
- At the start of work in this repo, read `项目记忆.md`.
- Keep decisions, structure, and roadmap aligned with it.
- Update `项目记忆.md` when the project's goals, structure, or major decisions change.

## Required Reading
- At the start of each new session in this repo, read `README.md`, `项目记忆.md`, and `docs/roadmap/README.md`.
- Before creating or updating docs, read the relevant `README.md` files in the related directories.
- Before reviewing or helping organize a topic, read the related files under `references/` if the user is using them as input.
- Keep new docs aligned with each file's stated responsibilities and non-responsibilities.

## Documentation Language
- Write all project documentation in Chinese, regardless of whether the conversation with the user is in Chinese or English.
- Keep English practice in the conversation, while storing durable project knowledge in Chinese for easier learning and review.

## Language Mode
- If the user's message contains more English words than Chinese words, first rephrase it into natural, idiomatic English for an international audience. Do not translate literally.
- Then answer in English in a natural style.
- If the user's message contains more Chinese words than English words, reply in Chinese.
- The purpose is to help the user learn more natural English expressions.
