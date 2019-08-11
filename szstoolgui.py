#!/usr/bin/env python3
# -*- coding: utf-8 -*-

# SARC Tool
# Version v0.4
# Copyright © 2017-2018 MasterVermilli0n / AboodXD

# GUI.py
# Author: Gigaboy-01 / Adam Oates
# Since: 5-15-2019

# This is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.

# This is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

################################################################
################################################################


from szstool import pack

import platform
import threading
import os
import shutil
from pathlib import Path

import tkinter as Tk
import tkinter.ttk as Ttk
from tkinter import filedialog
from tkinter import messagebox

# opens file dialog and sets an Tk.Entry object text to file source
def selFile(entryfield):
    src = Tk.filedialog.askopenfilename(filetypes=[("SZS, SARC", "*.szs *.sarc")])
    if src != None:
        entryfield.delete(0, Tk.END)
        entryfield.insert(0, src)


def saveFile(entryfield):
    src = Tk.filedialog.asksaveasfilename(filetypes=[("SZS", "*.szs"),("SARC", "*.sarc")])
    if src != None:
        entryfield.delete(0, Tk.END)
        entryfield.insert(0, src)


def selDir(entryfield):
    src = Tk.filedialog.askdirectory()
    if src != None:
        entryfield.delete(0, Tk.END)
        entryfield.insert(0, src)


# centers window on screen with X and Y offset
def center(win, offx=0, offy=0):
    win.update_idletasks()
    width = win.winfo_width()
    height = win.winfo_height()
    x = (win.winfo_screenwidth() // 2) - (width // 2)
    y = (win.winfo_screenheight() // 2) - (height // 2)
    win.geometry('{}x{}+{}+{}'.format(width, height, (x+offx), (y+offy)))
    
    def extract(file):
    
    with open(file, "rb") as inf:
        inb = inf.read()

    while yazLib.IsYazCompressed(inb):
        inb = yazLib.decompress(inb)

    name = os.path.splitext(file)[0]
    ext = szsLib.guessFileExt(inb)

    if ext != ".sarc":
        with open(''.join([name, ext]), "wb") as out:
            out.write(inb)

    else:
        arc = szsLib.SARC_Archive()
        arc.load(inb)

        root = os.path.join(os.path.dirname(file), name)
        if not os.path.isdir(root):
            os.mkdir(root)

        files = []

        def getAbsPath(folder, path):
            nonlocal root
            nonlocal files

            for checkObj in folder.contents:
                if isinstance(checkObj, szsLib.File):
                    files.append(["/".join([path, checkObj.name]), checkObj.data])

                else:
                    path_ = os.path.join(root, "/".join([path, checkObj.name]))
                    if not os.path.isdir(path_):
                        os.mkdir(path_)

                    getAbsPath(checkObj, "/".join([path, checkObj.name]))

        for checkObj in arc.contents:
            if isinstance(checkObj, szsLib.File):
                files.append([checkObj.name, checkObj.data])

            else:
                path = os.path.join(root, checkObj.name)
                if not os.path.isdir(path):
                    os.mkdir(path)

                getAbsPath(checkObj, checkObj.name)

        for file, fileData in files:
            print(file)
            with open(os.path.join(root, file), "wb") as out:
                out.write(fileData)




def GUI_extract(src, out):
    try:
        src = Path(src).resolve()
    except FileNotFoundError:
        messagebox.showerror("File Not Found", "The file at " + src + " does not exist.")
        return

    try:
        out = Path(out).resolve()
    except FileNotFoundError:
        messagebox.showerror("File Not Found", "The directory at " + out + " does not exist.")
        return

    extract(src)

    # if output directory is same directory as source, then we don't need to move the directory
    if str(os.path.split(src)[0]) != str(out):
        shutil.move(os.path.splitext(src)[0], out)

    messagebox.showinfo("Finished", "Extraction Complete.\nSource: " + str(src) + "\nOutput: " + str(out))


def GUI_pack(src, out, endianness, compr_lvl):
    try:
        src = Path(src).resolve()
    except FileNotFoundError:
        messagebox.showerror("File Not Found", "The directory at " + src + " does not exist.")
        return

    try:
        out = Path(out).resolve()
    except FileNotFoundError:
        messagebox.showerror("File Not Found", "The file at " + out + " does not exist.")
        return

    src = os.path.abspath(src)
    compr_lvl = int(float(compr_lvl))
    pack(src, endianness, compr_lvl, out)
    messagebox.showinfo("Finsihed", "Packing Complete\nSource: " + str(src) + "\nOutput: " + str(out))



def main():
    # initialize variables
    lbl_font = ("Times New Roman", 16)

    # instantiates the primary window
    primary = Tk.Tk()
    primary.title("SZSTool v0.5 GUI")
    primary.resizable(False, False)
    primary.option_add("*Dialog.msg.width", 600)

    # configure styles
    stl = Ttk.Style()

    if platform.system() == "Windows":
        stl.theme_use("vista")
    elif platform.system() == "Darwin":
        stl.theme_use("aqua")
    elif platform.system == "Linux":
        stl.theme_use("clam")

    stl.configure("TButton",
                  padding=3,
                  font=("Calibri", 11, "italic", "bold"))
    stl.configure("TEntry",
                  padding=[2, 1],
                  font=("Calibri", 11))

    tabCtrl = Ttk.Notebook(primary)

    ########## EXTRACT FRAME START ##########
    extract_frame = Ttk.Frame(tabCtrl)

    # source Label
    extract_src_lbl = Ttk.Label(extract_frame, text="Source File:", font=lbl_font)
    extract_src_lbl.grid(row=0, column=0, sticky="W", padx=8, pady=(10, 0))

    # source Entry
    extract_src_entry = Ttk.Entry(extract_frame, width=55)
    extract_src_entry.grid(row=1, column=0, padx=(10, 5), pady=3, ipady=3)

    # source Button
    extract_src_bttn = Ttk.Button(extract_frame, text="Select...", command=lambda: selFile(extract_src_entry))
    extract_src_bttn.grid(row=1, column=1, padx=(5, 10), pady=3)

    # output Label
    extract_out_lbl = Ttk.Label(extract_frame, text="Output Directory:", font=lbl_font)
    extract_out_lbl.grid(row=2, column=0, sticky="W", padx=8, pady=(10, 0))

    # output Entry
    extract_out_entry = Ttk.Entry(extract_frame, width=55)
    extract_out_entry.grid(row=3, column=0, padx=(10, 5), pady=3, ipady=3)

    #output Button
    extract_out_bttn = Ttk.Button(extract_frame, text="Select...", command=lambda: selDir(extract_out_entry))
    extract_out_bttn.grid(row=3, column=1, padx=(5, 10), pady=3)

    # extract file button
    extract_bttn = Ttk.Button(extract_frame, text="Extract", command=lambda: threading.Thread(target=GUI_extract, args=(extract_src_entry.get(), extract_out_entry.get())).start())
    extract_bttn.grid(row=4, column=0, sticky="W", padx=10, pady=(3, 6))
    ########## EXTRACT FRAME END ##########

    ########## PACK FRAME START #########
    pack_frame = Ttk.Frame(tabCtrl)

    # source Label
    pack_src_lbl = Ttk.Label(pack_frame, text="Source Directory:", font=lbl_font)
    pack_src_lbl.grid(row=0, column=0, sticky="W", padx=8, pady=(10, 0))

    # source Entry
    pack_src_entry = Ttk.Entry(pack_frame, width=55)
    pack_src_entry.grid(row=1, column=0, sticky="W", padx=(10, 5), pady=3, ipady=3)

    # source Button
    pack_src_bttn = Ttk.Button(pack_frame, text="Select...", command=lambda: selDir(pack_src_entry))
    pack_src_bttn.grid(row=1, column=1, sticky="W", padx=(5, 10), pady=3)

    # endianness Label
    endianness_lbl = Ttk.Label(pack_frame, text="Console:", font=lbl_font)
    endianness_lbl.grid(row=2, column=0, sticky="W", padx=8, pady=(10, 0))

    pack_rb_val = Tk.StringVar(None, '<')
    # Switch/3DS Radiobutton
    endianness_switch3ds_rb = Ttk.Radiobutton(pack_frame, text="Switch/3DS", value='<', variable=pack_rb_val)
    endianness_switch3ds_rb.grid(row=3, column=0, sticky="W", padx=8, pady=3)

    # Wii U Radiobutton
    endianness_wiiu_rb = Ttk.Radiobutton(pack_frame, text="Wii U", value='>', variable=pack_rb_val)
    endianness_wiiu_rb.grid(row=4, column=0, sticky="W", padx=8, pady=3)

    # compression level Label
    compr_lvl_lbl = Ttk.Label(pack_frame, text="Compression [0 - fastest/largest; 9 - slowest/smallest]:", font=lbl_font)
    compr_lvl_lbl.grid(row=5, column=0, columnspan=2, sticky="W", padx=8, pady=(10, 0))

    # compression level Spinbox
    compr_lvl_spnr = Ttk.Spinbox(pack_frame, from_=0, to=9, state="readonly", justify="center", font=("Serif", 11), width=10)
    compr_lvl_spnr.grid(row=6, column=0, sticky="W", padx=(10, 5), pady=3)

    # output Label
    pack_out_lbl = Ttk.Label(pack_frame, text="Output File:", font=lbl_font)
    pack_out_lbl.grid(row=7, column=0, sticky="W", padx=8, pady=(10, 0))

    # output Entry
    pack_out_entry = Ttk.Entry(pack_frame, width=55)
    pack_out_entry.grid(row=8, column=0, sticky="W", padx=(10, 5), pady=3, ipady=3)

    # output Button
    pack_out_bttn = Ttk.Button(pack_frame, text="Select...", command=lambda: saveFile(pack_out_entry))
    pack_out_bttn.grid(row=8, column=1, sticky="W", padx=(5, 10), pady=3)

    # pack Button
    pack_bttn = Ttk.Button(pack_frame, text="Pack", command=lambda: threading.Thread(target=GUI_pack, args=(pack_src_entry.get(), pack_out_entry.get(), pack_rb_val.get(), compr_lvl_spnr.get())).start())
    pack_bttn.grid(row=9, column=0, sticky="W", padx=10, pady=(3, 6))
    ########## PACK FRAME END ###########

    tabCtrl.add(extract_frame, text="Extract")
    tabCtrl.add(pack_frame, text="Pack")
    tabCtrl.pack(expand=True, fill="both")

    center(primary, 0, -50)
    Tk.mainloop()

if __name__ == '__main__':
    main()
