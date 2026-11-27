#!/usr/bin/env python3

import platform
import re
import subprocess
from pathlib import Path
import os
import stat
import logging
from typing import List, Tuple
import shutil

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


def replace_in_template(input_file, output_file, replacements: List[Tuple[str, str]]):
    logger.info(f"generating {output_file}")
    with open(f"templates/{input_file}", "r") as f:
        lines = f.readlines()
    new_lines = []
    for line in lines:
        new_line = line
        for placeholder, replacement in replacements:
            new_line = new_line.replace(placeholder, str(replacement))
        new_lines.append(new_line)
    with open(output_file, "w") as f:
        f.writelines(new_lines)


def make_executable(filename):
    st = os.stat(filename)
    os.chmod(filename, st.st_mode | stat.S_IXUSR)

if not check_valid_financial_year(year):
    logger.error("Please enter a valid financial year in the format like (2026-27)")
    exit(1)

def generate_desktop_entry():
    logger.info("generating desktop entry")
    destination_filename = f"OpenConsignment {year}.desktop"
    replacements = [("{{PLACEHOLDER_YEAR}}", year), ("{{PLACEHOLDER_APP_PATH}}", Path.cwd().resolve())]
    replace_in_template(
        "OpenConsignment.template.desktop", destination_filename, replacements
    )
    make_executable(destination_filename)

    source = Path(destination_filename)
    destination = Path.home() / 'Desktop' / destination_filename
    shutil.copy2(source, destination)

os_name = platform.system()

if os_name != "Linux":
    logger.error(f"Created desktop entries are only supported in Linux / Ubuntu Distro")
    exit(1)

generate_desktop_entry()