#!/usr/bin/env python3

import platform
import re
import subprocess
from pathlib import Path
import os
import stat
import logging

logging.basicConfig(
    level=logging.DEBUG, format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

year = str(input("Enter financial year in the format like (2026-27): "))


def check_valid_financial_year(year: str):
    if not re.fullmatch(r"\d{4}-\d{2}", year):
        return False
    start_year = year.split("-")[0][2:]
    end_year = year.split("-")[1]
    return int(start_year) + 1 == int(end_year)


def replace_in_template(input_file, output_file, placeholder, replacement):
    logger.info(f"generating {output_file}")
    with open(f"templates/{input_file}", "r") as f:
        lines = f.readlines()
    new_lines = []
    for line in lines:
        new_line = line.replace(placeholder, replacement)
        new_lines.append(new_line)
    with open(output_file, "w") as f:
        f.writelines(new_lines)


def make_executable(filename):
    st = os.stat(filename)
    os.chmod(filename, st.st_mode | stat.S_IXUSR)


def generate_bash_scripts():
    logger.info("generating bash scripts")
    replace_in_template(
        "download.template.sh", "download.sh", "{{PLACEHOLDER_YEAR}}", year
    )
    replace_in_template("upload.template.sh", "upload.sh", "{{PLACEHOLDER_YEAR}}", year)
    make_executable("download.sh")
    make_executable("upload.sh")
    make_executable("start.sh")


def generate_batch_scripts():
    logger.info("generating batch scripts")
    replace_in_template(
        "download.template.bat", "download.bat", "{{PLACEHOLDER_YEAR}}", year
    )
    replace_in_template(
        "upload.template.bat", "upload.bat", "{{PLACEHOLDER_YEAR}}", year
    )


def init_github_repository():
    repo_name = f"Satyam-Database-{year}"
    if (Path(repo_name) / ".git").is_dir():
        logger.info(f"{repo_name} already exists, so skipping clone")
        return
    github_username = "drcbabu81"
    repo_url = f"git@github.com:{github_username}/{repo_name}.git"

    try:
        logger.info(f"cloning {repo_name}")
        subprocess.run(
            ["git", "clone", repo_url], check=True, capture_output=True, text=True
        )

        if (Path(repo_name) / ".git").is_dir():
            logger.info("clone successful")
        else:
            logger.error("git returned success but .git directory is missing")

    except subprocess.CalledProcessError as e:
        logger.error("clone failed")
        logger.error(e.stderr)


if not check_valid_financial_year(year):
    logger.error("Please enter a valid financial year in the format like (2026-27)")
    exit(0)

os_name = platform.system()
init_github_repository()

if os_name == "Windows":
    generate_batch_scripts()
elif os_name == "Darwin":
    generate_bash_scripts()
elif os_name == "Linux":
    generate_bash_scripts()
else:
    logger.error(f"Unknown OS: {os_name}")
    exit(1)
