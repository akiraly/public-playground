@echo off
setlocal enabledelayedexpansion

REM List of absolute paths to Git project directories
set DIRS=^
"C:\Projects\Repo1" ^
"D:\Work\Repo2" ^
"E:\Dev\Repo3"

for %%D in (%DIRS%) do (
    echo.
    echo ===============================
    echo Entering %%~D
    echo ===============================
    pushd %%~D

    REM Ensure it's a Git repository
    if exist ".git" (
        echo Fetching from remote...
        git fetch origin

        REM Get current branch name
        for /f "delims=" %%B in ('git rev-parse --abbrev-ref HEAD') do set BRANCH=%%B

        if /i "!BRANCH!"=="master" (
            echo On 'master' branch — skipping rebase.
        ) else if /i "!BRANCH!"=="main" (
            echo On 'main' branch — skipping rebase.
        ) else (
            REM Check for uncommitted changes
            git diff --quiet && git diff --cached --quiet
            if errorlevel 1 (
                echo Stashing local changes...
                git stash push -u -m "Auto stash before rebase"
                set STASHED=1
            ) else (
                set STASHED=0
            )

            echo Rebasing current branch "!BRANCH!" onto origin/master...
            git rebase origin/master

            if "!STASHED!"=="1" (
                echo Unstashing previous changes...
                git stash pop
            )
        )
    ) else (
        echo Not a Git repository: %%~D
    )

    popd
)

echo.
echo All done.
pause
