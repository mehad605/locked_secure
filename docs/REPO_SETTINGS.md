# Recommended GitHub Repository Security Settings Checklist

The items below live in GitHub repository settings (outside Git) and must be applied manually by the repository owner (`@mehad605`).

---

## 1. Protected `release` Environment (Required for `release.yml`)
- [ ] Go to **Settings → Environments → New environment**, name it **`release`**.
- [ ] Enable **Required reviewers** and add `@mehad605`.
- [ ] Under **Deployment branches and tags**, select **Selected branches and tags** and add tag rule **`v*`**.
- [ ] Move the four signing secrets from repository-level secrets into the **`release`** environment:
  - `SIGNING_KEY`
  - `SIGNING_STORE_PASSWORD`
  - `SIGNING_KEY_ALIAS`
  - `SIGNING_KEY_PASSWORD`
- [ ] Delete the old repository-level copies of those four secrets once the `release` environment is verified.

## 2. Branch Protection (`main`)
- [ ] Go to **Settings → Rules → Rulesets** (or **Branches → Add branch protection rule**) for `main`:
  - [ ] Require a pull request before merging
  - [ ] Require approvals (at least 1 review; require review from Code Owners via `.github/CODEOWNERS`)
  - [ ] Dismiss stale pull request approvals when new commits are pushed
  - [ ] Require status checks to pass before merging: `Format, Static Analysis, Lint & Test` and `Dependency Review`
  - [ ] Require signed commits (`git commit -S`)
  - [ ] Block force pushes and deletions

## 3. Tag Protection (`v*`)
- [ ] Go to **Settings → Rules → Rulesets → New tag ruleset** targeting `v*`:
  - [ ] Restrict tag creation/updates/deletions to repository administrators only
  - [ ] Require signed tags (`git tag -s`)

## 4. Code Security & Supply Chain
- [ ] Go to **Settings → Code security**:
  - [ ] Enable **Dependency graph**
  - [ ] Enable **Dependabot alerts** and **Dependabot security updates** (`.github/dependabot.yml` is already committed)
  - [ ] Enable **Secret scanning**
  - [ ] Enable **Push protection** (blocks accidental pushes containing credentials or private keys)

## 5. Actions Security
- [ ] Go to **Settings → Actions → General**:
  - [ ] Under **Workflow permissions**, set default `GITHUB_TOKEN` permissions to **Read repository contents and packages permissions**
  - [ ] Uncheck **Allow GitHub Actions to create and approve pull requests**
  - [ ] Under **Fork pull request workflows**, require approval for all outside collaborators
