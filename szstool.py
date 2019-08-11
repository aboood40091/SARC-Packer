import os
import sys
import time
import szsLib
import yazLib

def pause():
    programPause = input("\nPress the <ENTER> key to exit...\n\n")

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


def pack(root, endianness, level, outname):

    if "\\" in root:
        root = "/".join(root.split("\\"))

    if root[-1] == "/":
        root = root[:-1]

    arc = szsLib.SARC_Archive(endianness=endianness)
    lenroot = len(root.split("/"))

    for path, dirs, files in os.walk(root):
        if "\\" in path:
            path = "/".join(path.split("\\"))

        lenpath = len(path.split("/"))

        if lenpath == lenroot:
            path = ""

        else:
            path = "/".join(path.split("/")[lenroot - lenpath:])

        for file in files:
            if path:
                filename = ''.join([path, "/", file])

            else:
                filename = file

            print(filename)

            fullname = ''.join([root, "/", filename])

            i = 0
            for folder in filename.split("/")[:-1]:
                if not i:
                    exec("folder%i = szsLib.Folder(folder + '/'); arc.addFolder(folder%i)".replace('%i', str(i)))

                else:
                    exec("folder%i = szsLib.Folder(folder + '/'); folder%m.addFolder(folder%i)".replace('%i', str(i)).replace('%m', str(i - 1)))

                i += 1

            with open(fullname, "rb") as f:
                inb = f.read()

            hasFilename = True
            if file[:5] == "hash_":
                hasFilename = False

            if not i:
                arc.addFile(szsLib.File(file, inb, hasFilename))

            else:
                exec("folder%m.addFile(szsLib.File(file, inb, hasFilename))".replace('%m', str(i - 1)))

    data, maxAlignment = arc.save()

    if level != -1:
        outData = yazLib.compress(data, maxAlignment, level)
        del data

        if not outname:
            outname = ''.join([root, ".szs"])

    else:
        outData = data
        if not outname:
            outname = ''.join([root, ".sarc"])

    with open(outname, "wb+") as output:
        output.write(outData)


def printInfoen():
    print("Usage:")
    print("  szstool.py -en [option...] file/folder")
    print("\nPacking Options:")
    print(" -o <output>    Output file name (Optional)")
    print(" -l             Output will be in little endian if this is used")
    print(" -c <level>     Yaz0 (SZS) compress the output with the specified level (0-9)")
    print("                0: No compression (Fastest)")
    print("                9: Best compression (Slowest)")
    pause()
    sys.exit(1)
    
def printInfofr():
    print("Usage:")
    print("  szstool.py -fr [option...] dossier/chemise")
    print("\nOptions d’emballage:")
    print(" -o <output>    Nom du fichier de sortie (facultatif)")
    print(" -l             La sortie sera en petit endian si elle est utilisée")
    print(" -c <niveau>    Yaz0 (SZS) compresser la sortie avec le niveau spécifié (0-9)")
    print("                0: pas de compression (la plus rapide)")
    print("                9: meilleure compression (la plus lente)")
    pause()
    sys.exit(1)
    
def printInfopg():
    print("Uso:")
    print("  szstool.py -pg [opção...] arquivo/pasta")
    print("\nOpções de embalagem:")
    print(" -o <saída>     Nome do arquivo de saída (opcional)")
    print(" -l             A saída será em little endian se isso for usado")
    print(" -c <nível>     Yaz0 (SZS) comprime a saída com o nível especificado (0-9)")
    print("                0: sem compressão (mais rápido)")
    print("                9: Melhor compactação (mais lenta)")
    pause()
    sys.exit(1)
    
def printInfoes():
    print("Uso:")
    print("  szstool.py -es [opción...] archivo/carpeta")
    print("\nOpciones de embalaje:")
    print(" -o <salida>    Nombre del archivo de salida (Opcional)")
    print(" -l             La salida será en little endian si se usa")
    print(" -c <nivel>     Yaz0 (SZS) comprime la salida con el nivel especificado (0-9)")
    print("                0: Sin compresión (la más rápida)")
    print("                9: La mejor compresión (la más lenta)")
    pause()
    sys.exit(1)


def en():
    print("SZSTool v1.2")
    print("(C) 2018 CVFD / CVFireDragon\n")

    if len(sys.argv) < 2:
        printInfoen()

    root = os.path.abspath(sys.argv[-1])
    if os.path.isfile(root):
        extract(root)

    elif os.path.isdir(root):
        endianness = '>'
        level = -1

        if "-l" in sys.argv:
            endianness = '<'

        if "-c" in sys.argv:
            try:
                level = int(sys.argv[sys.argv.index("-compress") + 1], 0)

            except ValueError:
                level = 1

            if not 0 <= level <= 9:
                print("Invalid compression level!\n")
                sys.exit(1)

        if "-o" in sys.argv:
            outname = sys.argv[sys.argv.index("-o") + 1]

        else:
            outname = ""

        pack(root, endianness, level, outname)

    else:
        print("File/Folder doesn't exist!")
        sys.exit(1)
        
def fr():
    print("SZSTool v1.2")
    print("(C) 2018 CVFD / CVFireDragon\n")

    if len(sys.argv) < 2:
        printInfofr()

    root = os.path.abspath(sys.argv[-1])
    if os.path.isfile(root):
        extract(root)

    elif os.path.isdir(root):
        endianness = '>'
        level = -1

        if "-l" in sys.argv:
            endianness = '<'

        if "-c" in sys.argv:
            try:
                level = int(sys.argv[sys.argv.index("-compress") + 1], 0)

            except ValueError:
                level = 1

            if not 0 <= level <= 9:
                print("Niveau de compression non valide!\n")
                sys.exit(1)

        if "-o" in sys.argv:
            outname = sys.argv[sys.argv.index("-o") + 1]

        else:
            outname = ""

        pack(root, endianness, level, outname)

    else:
        print("Fichier/dossier n’existe pas!")
        sys.exit(1)
        
def pg():
    print("SZSTool v1.2")
    print("(C) 2018 CVFD / CVFireDragon\n")

    if len(sys.argv) < 2:
        printInfopg()

    root = os.path.abspath(sys.argv[-1])
    if os.path.isfile(root):
        extract(root)

    elif os.path.isdir(root):
        endianness = '>'
        level = -1

        if "-l" in sys.argv:
            endianness = '<'

        if "-c" in sys.argv:
            try:
                level = int(sys.argv[sys.argv.index("-compress") + 1], 0)

            except ValueError:
                level = 1

            if not 0 <= level <= 9:
                print("Nível de compactação inválido!\n")
                sys.exit(1)

        if "-o" in sys.argv:
            outname = sys.argv[sys.argv.index("-o") + 1]

        else:
            outname = ""

        pack(root, endianness, level, outname)

    else:
        print("Arquivo / Pasta não existe!")
        sys.exit(1)
        
def es():
    print("SZSTool v1.2")
    print("(C) 2018 CVFD / CVFireDragon\n")

    if len(sys.argv) < 2:
        printInfoes()

    root = os.path.abspath(sys.argv[-1])
    if os.path.isfile(root):
        extract(root)

    elif os.path.isdir(root):
        endianness = '>'
        level = -1

        if "-l" in sys.argv:
            endianness = '<'

        if "-c" in sys.argv:
            try:
                level = int(sys.argv[sys.argv.index("-compress") + 1], 0)

            except ValueError:
                level = 1

            if not 0 <= level <= 9:
                print("Nivel de compresión no válido!\n")
                sys.exit(1)

        if "-o" in sys.argv:
            outname = sys.argv[sys.argv.index("-o") + 1]

        else:
            outname = ""

        pack(root, endianness, level, outname)

    else:
        print("Archivo / Carpeta no existe!")
        sys.exit(1)

if __name__ == '__main__': en()
