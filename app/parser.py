import os
import sys


# GL Shader keywords.
IN = "in "
OUT = "out"
UNIFORM = "uniform"
VEC = "vec"
MAT = "mat"
FLOAT = "float"
INT = "int"
ARRAY = "[]"
TEXTURE = "sampler2D"
SAMPLER = "sampler1D"
BUFFER = "samplerBuffer"
VERTEX_SHADER_SUFFIX = "vsh"
FRAGMENT_SHADER_SUFFIX = "fsh"
HIGH_PRECISION = "highp"
MED_PRECISION = "mediump"
LOW_PRECISION = "lowp"
PRECISIONS = {HIGH_PRECISION, MED_PRECISION, LOW_PRECISION}

# Own defined keywords.
SPECTRUM = "spectrum"
SCHEME = "scheme"
TIME = "iTime"

# Kotlin classes equivalent of GL primitives.
KT_FLOAT = "Float"
KT_INT = "Int"
KT_MAT = "FloatArray"
DECLARATION_SEP = ", "
KT_TEXTURE = "Bitmap"
KT_SAMPLER = "FloatArray"

# Kotlin interfaces names.
SPECTRUM_INTERFACE = "SpectrumShader"
SCHEME_INTERFACE = "SchemeShader"
TIME_INTERFACE = "TimedShader"


def decompose_declaration(line):
    """
    Decomposes a shader variable declaration line into a tuple of components. e.g the line
    "uniform float floatUniform;"
    would be decomposed into and returned as the tuple:
    ("float", "floatUniform", False)
    :param line: A line declaring a variable.
    :return: A tuple of the declared type and name.
    """
    _, gl_type, name = list(filter(lambda declaration_component:
                                   declaration_component not in PRECISIONS,
                                   line.strip().split(" ")))
    name = name[:-1]  # Trim the enclosing ';'.
    if '[' in name:
        gl_type += ARRAY
        name = name[:name.find('[')]
    return gl_type, name, False


def decompose_string_format(line):
    """
    Decomposes a declaration containing a kotlin string format.
    "float shaderVarName = $compiledShaderVarName;"
    will be decomposed to
    ("float", "compiledShaderVarName", True)
    :param line:
    :return:
    """
    split_line = line.strip().split(" ")
    return split_line[0], split_line[3][1:-1], True


def gl_type_to_kotlin_class(gl_type: str):
    if gl_type == BUFFER:
        return KT_MAT
    if gl_type == INT:
        return KT_INT
    if gl_type == FLOAT:
        return KT_FLOAT
    if gl_type == TEXTURE:
        return KT_TEXTURE
    if gl_type == SAMPLER:
        return KT_SAMPLER
    if gl_type.startswith(VEC) or gl_type.startswith(MAT) or gl_type == FLOAT + ARRAY:
        return KT_MAT
    raise RuntimeError(f"No corresponding type for GL type: {gl_type}")


def parse_uniform_to_val(uniform: tuple):
    """
    Parses a tuple of uniform declaration in the form (type, name)
    :param uniform:
    :return:
    """
    return f"{uniform[1]}: {gl_type_to_kotlin_class(uniform[0])}"


def write_constructor(strings_decomposed, uniforms_decomposed):
    uniforms = DECLARATION_SEP.join(map(parse_uniform_to_val,
                                        strings_decomposed + uniforms_decomposed))
    uniforms_declaration = DECLARATION_SEP.join(map(uniform_declaration, uniforms_decomposed))
    return f"constructor({uniforms}): this(arrayOf({uniforms_declaration}))"


def write_class_declaration(class_name: str, is_vertex, is_spectrum, is_scheme, is_time):
    interfaces = []
    if is_vertex:
        interfaces.append("VertexShader")
    if is_spectrum:
        interfaces.append(SPECTRUM_INTERFACE)
    if is_scheme:
        interfaces.append(SCHEME_INTERFACE)
    if is_time:
        interfaces.append(TIME_INTERFACE)
    interface = ", ".join(interfaces) if len(interfaces) > 0 else "Shader"
    return f"class {class_name}(override val uniforms: Array<Uniform>) : {interface} "


def uniform_declaration(uniform):
    return f"Uniform(\"{uniform[1]}\", \"{uniform[0]}\", {uniform[1]})"


def write_var(uniform_e_tuple):
    readonly = uniform_e_tuple[1][2]
    index = uniform_e_tuple[0]
    name = uniform_e_tuple[1][1]
    is_spectrum = name == SPECTRUM
    is_scheme = name == SCHEME
    is_time = name == TIME
    override = "override " if is_scheme or is_spectrum or is_time else ""
    if is_scheme:
        as_declaration = " as " + KT_TEXTURE
    elif is_spectrum:
        as_declaration = " as " + KT_MAT
    elif is_time:
        as_declaration = " as " + KT_FLOAT
    else:
        as_declaration = ""
    var_dec = f"{override}{'val' if readonly else 'var'} {uniform_e_tuple[1][1]} " \
              + write_getter(index) + as_declaration
    return var_dec if readonly else var_dec + f"\n\t\t{write_setter(index)}"


def write_setter(index):
    uniform = f"uniforms[{index}]"
    setter = f"set(value) {{\n\t\t\t{uniform}.value = value\n\t\t\t"
    return setter + f"ShaderManager.setValueForShader(name, {uniform}.name, value)\n\t\t}}"


def write_getter(index):
    return f"get() = uniforms[{index}].value"


def write_mapper(uniforms):
    uniform_map = map(lambda uniform_e: f"\"{uniform_e[1][1]}\" to uniforms[{uniform_e[0]}]",
                      enumerate(uniforms))
    return "private val uniform_map = mapOf(" + ",".join(uniform_map) + ")"


def resource_getter(shader_name):
    return f"override val resource get() = R.raw.{shader_name}"


def inputs_getter(inputs):
    return f"override val inputs get() = arrayOf(" \
           f"{DECLARATION_SEP.join(map(uniform_declaration, inputs))})"


def remove_comments(line):
    comment_index = line.find('//')
    if comment_index == -1:
        return line
    uncommented = line[:comment_index]
    if uncommented == "\n": # Comment line
        return ""
    return uncommented


def write_name(name):
    return f"override val name = \"{name}\""


def parse_shader(file: str):
    with open(os.path.join(shader_folder_path, file), "r") as shader:
        lines = list(map(remove_comments, shader.readlines()))
        shader_name, shader_type = file.split('.')
        is_vertex = shader_type == VERTEX_SHADER_SUFFIX
        uniforms_decomposed = list(map(decompose_declaration,
                                       filter(lambda line: line.startswith(UNIFORM), lines)))
        strings_decomposed = list(map(decompose_string_format, filter(lambda line: '$' in line and
                                                                      not line.startswith(UNIFORM),
                                      lines)))
        # inputs_decomposed = list(map(decompose_declaration, filter(lambda line: line.startswith(IN),
        #                                                            lines))) if is_vertex else []
        is_spectrum = any(map(lambda uniform: uniform[1] == SPECTRUM, uniforms_decomposed))
        is_scheme = any(map(lambda uniform: uniform[1] == SCHEME, uniforms_decomposed))
        is_time = any(map(lambda uniform: uniform[1] == TIME, uniforms_decomposed))
        class_declaration = write_class_declaration(shader_name, is_vertex, is_spectrum, is_scheme,
                                                    is_time)
        raw_declaration = "override var rawString = " + "\"\"\"" + "".join(lines) + "\"\"\""
        getters = map(write_var, enumerate(uniforms_decomposed))
        declaration_array = [write_constructor(strings_decomposed, uniforms_decomposed),
                             write_name(shader_name),
                             *getters,
                             raw_declaration]
        # if is_vertex:
        #     declaration_array.append(inputs_getter(inputs_decomposed))
        body = "\n\t".join(declaration_array)
        output.write("\n" + class_declaration + "{\n\t" + body + "\n}\n")


def shader_interface_declaration():
    uniform = "val uniforms: Array<Uniform>"
    resource = "val resource: Int"
    name = "val name: String"
    raw_string = "var rawString: String"
    return "interface Shader {\n\t" + "\n\t".join([uniform, name, raw_string]) + "\n}\n"


def spectrum_shader_declaration():
    spectrum = "var spectrum: FloatArray"
    return f"interface {SPECTRUM_INTERFACE} : Shader {{\n\t" + spectrum + "\n}\n"


def scheme_shader_declaration():
    scheme = "var scheme: Bitmap"
    return f"interface {SCHEME_INTERFACE} : Shader {{\n\t" + scheme + "\n}\n"


def time_shader_declaration():
    time = "var iTime: Float"
    return f"interface {TIME_INTERFACE} : Shader {{\n\t" + time + "\n}\n"


if __name__ == "__main__":
    shader_folder_path = sys.argv[1]
    if shader_folder_path == "-h" or shader_folder_path == "--help":
        print("""This script accepts 2 unnamed arguments in the following order:
        \tshader_folder_path: The path relative or absolute path to the folder where shaders are 
        stored. Most commonly in the relative path 'app\\src\\main\\res\\raw'
        output_path: Path to the kotlin file where the compiled shaders should be written to.
        """)
    destination_file_path = sys.argv[2]
    with open(destination_file_path, 'w') as output:
        warning = "// WARNING\n// This file was auto generated by parser.py\n// " + \
                  "Do not edit manually."
        imports = "package com.dishtech.vgg.shaders\n\nimport android.graphics.Bitmap\n"
        vertex_declaration = "interface VertexShader : Shader {\n\tval inputs: Array<Uniform>\n}\n"
        output.write("\n".join([warning,
                                imports,
                                shader_interface_declaration(),
                                spectrum_shader_declaration(),
                                scheme_shader_declaration(),
                                time_shader_declaration()]))
        for f in os.listdir(shader_folder_path):
            if f.endswith(FRAGMENT_SHADER_SUFFIX):
                parse_shader(f)
