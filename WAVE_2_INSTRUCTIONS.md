# Brief App - Wave 2 Micro-Agents (015-019)

## Overview

Wave 2 agents are ready for execution! These 5 agents build on the completed Phases 0-10 modernization to add production-ready features and optimizations.

## Agent Summary

| Agent | Task | Priority | Dependencies |
|-------|------|----------|-------------|
| **015** | CI/CD Pipeline Setup | High | None - can run first |
| **016** | Performance Optimization | High | None - can run in parallel with 015 |
| **017** | Home Screen Widget | Medium | None - can run in parallel |
| **018** | Multiple News Sources | Medium | None - can run in parallel |
| **019** | Analytics & Monitoring | Low | Requires Firebase setup |

## Execution Instructions for Remote Agent (Claude Code for Web)

For each agent, use this workflow:

### Step 1: Create Agent Branch
```bash
# Local terminal - for agent 015
git fetch --all
git checkout master
git pull origin master
```

### Step 2: Remote Agent Execution

**Copy this prompt to Claude Code for Web:**

```
Fetch and pull on master branch. Read .claude/agents/015-ci-cd-pipeline.md and follow the instructions exactly. Create all files as specified, verify the build compiles, then push to a new branch named claude/ci-cd-pipeline-015{UNIQUE_ID}.
```

Replace `015-ci-cd-pipeline.md` and `claude/ci-cd-pipeline-015` with the appropriate agent number/name for each agent.

### Step 3: Local Integration
```bash
# After remote agent completes
cd ~/StudioProjects/Brief
git fetch --all

# Review the changes
git log origin/claude/ci-cd-pipeline-015{UNIQUE_ID} -3
git diff master..origin/claude/ci-cd-pipeline-015{UNIQUE_ID}

# Integrate via merge
git checkout master
git merge origin/claude/ci-cd-pipeline-015{UNIQUE_ID} --no-edit

# Verify build
./gradlew assembleDebug

# Push and cleanup
git push origin master
git push origin --delete claude/ci-cd-pipeline-015{UNIQUE_ID}
```

## Agent Details

### 015: CI/CD Pipeline Setup ⚡ HIGH PRIORITY
**Prompt for Remote Agent:**
```
Fetch and pull on master branch. Read .claude/agents/015-ci-cd-pipeline.md and follow exactly. Create GitHub Actions workflows for CI, releases, and PR checks. Add JaCoCo test coverage reporting. Verify the configuration is valid, then push to branch claude/ci-cd-pipeline-015{UNIQUE_ID}.
```

**What it does:**
- GitHub Actions CI workflow (test, lint, build)
- Release workflow with automated APK generation
- PR validation with test coverage reporting
- Dependabot for dependency updates

**Expected outcome:** Automated testing and builds on every commit

---

### 016: Performance Optimization ⚡ HIGH PRIORITY
**Prompt for Remote Agent:**
```
Fetch and pull on master branch. Read .claude/agents/016-performance-optimization.md and follow exactly. Enable R8/ProGuard, add network security config, configure StrictMode for debug builds, and add performance dependencies. Verify release build compiles successfully, then push to branch claude/performance-optimization-016{UNIQUE_ID}.
```

**What it does:**
- R8/ProGuard for code shrinking (30-40% APK size reduction)
- Network security configuration
- StrictMode for debug builds
- LeakCanary integration
- ProGuard rules for all libraries

**Expected outcome:** Smaller, faster, more secure release builds

---

### 017: Home Screen Widget
**Prompt for Remote Agent:**
```
Fetch and pull on master branch. Read .claude/agents/017-home-screen-widget.md and follow exactly. Implement Glance-based home screen widget showing latest news articles. Create widget UI, repository, receiver, and worker. Verify app compiles, then push to branch claude/home-screen-widget-017{UNIQUE_ID}.
```

**What it does:**
- Glance-based modern widget
- Shows 5 latest news articles
- Updates every hour via WorkManager
- Material3 styling
- Tapping widget opens app

**Expected outcome:** Users can add news widget to home screen

---

### 018: Multiple News Sources
**Prompt for Remote Agent:**
```
Fetch and pull on master branch. Read .claude/agents/018-multiple-news-sources.md and follow exactly. Add support for multiple Wikipedia sections (Current Events, In The News, On This Day) and 10 languages. Create source selection UI, update database schema, extend repository. Verify app compiles and migrations work, then push to branch claude/multiple-sources-018{UNIQUE_ID}.
```

**What it does:**
- Support for 5 Wikipedia news sections
- Support for 10 languages (English, Spanish, French, German, Japanese, Chinese, Russian, Portuguese, Italian, Arabic)
- User can select which sources to enable
- Database migration from v2 to v3
- Source badges on articles

**Expected outcome:** Users can customize news feed with multiple sources

---

### 019: Analytics & Monitoring ⚠️ REQUIRES FIREBASE SETUP
**Prompt for Remote Agent:**
```
Fetch and pull on master branch. Read .claude/agents/019-analytics-monitoring.md and follow exactly. Integrate Firebase Analytics and Crashlytics. Create AnalyticsManager, add event tracking throughout app, update settings for privacy controls. NOTE: This requires google-services.json file - the agent should create stub code that compiles without Firebase. Push to branch claude/analytics-monitoring-019{UNIQUE_ID}.
```

**What it does:**
- Firebase Analytics for usage tracking
- Crashlytics for crash reporting
- Track article views, searches, bookmarks, shares
- Track screen views and user flows
- Privacy controls in settings
- No PII collected

**Important:** Requires Firebase project setup and `google-services.json` file

**Expected outcome:** Usage analytics and crash reporting

---

## Parallel Execution Strategy

You can run multiple agents in parallel to speed up development:

**Batch 1 (Independent - Run in Parallel):**
- Agent 015 (CI/CD)
- Agent 016 (Performance)
- Agent 017 (Widget)

**Batch 2 (After Batch 1 completes):**
- Agent 018 (Multiple Sources)

**Batch 3 (Requires manual Firebase setup):**
- Agent 019 (Analytics) - Do this last after Firebase project is created

## Firebase Setup for Agent 019

Before running agent 019:

1. Go to https://console.firebase.google.com
2. Create new project "Brief"
3. Add Android app with package: `com.techventus.wikipedianews`
4. Download `google-services.json`
5. Place in `app/` directory
6. **DO NOT** commit `google-services.json` to git (already in .gitignore)

## Verification Checklist

After all agents complete:

- [ ] GitHub Actions workflows running on every commit
- [ ] Release APK size reduced by 30-40%
- [ ] ProGuard mapping files generated
- [ ] Home screen widget can be added
- [ ] Multiple news sources available in settings
- [ ] 10 languages supported
- [ ] Firebase Analytics tracking events (if configured)
- [ ] Crashlytics reporting crashes (if configured)
- [ ] All tests passing
- [ ] App builds and runs successfully

## Success Metrics

After Wave 2 completion:

| Metric | Before | Target | Status |
|--------|--------|--------|--------|
| APK Size | ~15MB | ~10MB | ⏳ |
| Test Coverage | 70% | 80%+ | ⏳ |
| Build Automation | Manual | Automated | ⏳ |
| News Sources | 1 | 50+ combinations | ⏳ |
| Languages | 1 (English) | 10 languages | ⏳ |
| Widget Support | No | Yes | ⏳ |
| Analytics | No | Yes | ⏳ |

## Next Steps After Wave 2

Once all Wave 2 agents are complete, consider:

1. **User Testing**: Beta test with real users
2. **Play Store Release**: Prepare store listing and screenshots
3. **Documentation**: Update README with new features
4. **Marketing**: Create promotional materials
5. **Wave 3 Planning**: Plan next feature set (user accounts, ML, etc.)

## Support

If any agent encounters issues:

1. Check the agent's completion marker: `cat .claude/completed/015`
2. Review build logs: `./gradlew assembleDebug`
3. Check git branches: `git branch -r | grep claude/`
4. Review agent instruction file for troubleshooting steps

---

**Ready to build production-ready features! 🚀**
