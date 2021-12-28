#!python3
import subprocess
import argparse
from subprocess import Popen, PIPE

parser = argparse.ArgumentParser(description='auto marking')
parser.add_argument('--not-compile', action='store_true', default=False)
args = parser.parse_args()

BUILD_FLAG = not args.not_compile

class MODE:
    LEXER = "lexer"
    SEM = "sem"
    AST = "ast"
    PARSER = "parser"
    GEN = "gen"

class STATUS:
    FAIL = "FAIL"
    PASS = "PASS"

# build subprocess' run
class Build():
    def __init__(self, path, expected_codes, modes=[MODE.LEXER, MODE.PARSER, MODE.AST, MODE.SEM, MODE.GEN], mars_callback = None):
        self.path = path
        if not isinstance(expected_codes, list):
            assert(isinstance(expected_codes, int))
            self.expected_codes = [expected_codes] * len(modes)
        else:
            self.expected_codes = expected_codes
        assert(len(self.expected_codes) == len(modes))
        self.result = []
        self.modes = modes
        self.mars_callback = mars_callback

    def run(self):
        for i in range(len(self.modes)):
            mode = self.modes[i]
            expected_code = self.expected_codes[i]

            cmd = ["java", "-cp", "bin" , "Main" ,"-{}".format(mode), self.path, "dummy.out"]
            pc = subprocess.run(cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

            status_msg = STATUS.FAIL if pc.returncode != expected_code else STATUS.PASS
            msg = "{}, path: {}, return: {}, expected: {} | {}".format(status_msg, self.path, pc.returncode, expected_code, ' '.join(cmd))
            self.result.append((pc.returncode == expected_code, msg))

            if mode == MODE.GEN and pc.returncode == 0:
                cmd = ["java", "-jar", "lib/Mars4_5.jar", "nc", "dummy.out"]
                if (self.mars_callback != None):
                    pc = Popen(cmd, stdin = PIPE, stdout = PIPE)
                    is_pass = self.mars_callback(pc)
                    status_msg = STATUS.FAIL if not is_pass else STATUS.PASS
                else:
                    pc = subprocess.run(cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
                    status_msg = STATUS.FAIL if pc.returncode != expected_code else STATUS.PASS
                msg = "{}, path: {}, return: {}, expected: {} | {}".format(status_msg, self.path, pc.returncode, expected_code, ' '.join(cmd))
                self.result.append((pc.returncode == expected_code, msg))



    def __repr__(self):
        return '\n'.join(list(map(lambda _: _[1], self.result)))

    @property
    def succ_count(self):
        return sum(list(zip(*self.result))[0])

    @property
    def fail_count(self):
        return len(self.result) - sum(list(zip(*self.result))[0])

    @property
    def count(self):
        return len(self.result)

if __name__ == '__main__':
    if BUILD_FLAG:
        subprocess.run(["ant", "build"]) # build

    def binary_search():
        def mars_callback(p):
            out, err = p.communicate()
            return out.decode('utf-8') == "12345-19999\n"
        return Build("tests/binary_search.c", 0, [MODE.GEN], mars_callback)

    def recur():
        def mars_callback(p):
            out, err = p.communicate()
            return out.decode('utf-8') == "12\n"
        return Build("tests/recur.c", 0, [MODE.GEN], mars_callback)

    def fibonacci():
        def mars_callback(p):
            p.stdin.write(b"12\n")
            out, err = p.communicate()
            return out.decode('utf-8') == "First 12 terms of Fibonacci series are : 0 1 1 2 3 5 8 13 21 34 55 89 \n"
        return Build("tests/fibonacci.c", 0, mars_callback = mars_callback)

    def tictactoe():
        def mars_callback(p):
            p.stdin.write(b"a\n")
            p.stdin.write(b"1\n")
            p.stdin.write(b"b\n")
            p.stdin.write(b"1\n")
            p.stdin.write(b"b\n")
            p.stdin.write(b"2\n")
            p.stdin.write(b"c\n")
            p.stdin.write(b"2\n")
            p.stdin.write(b"c\n")
            p.stdin.write(b"3\n")
            p.stdin.write(b"n\n")
            out, err = p.communicate()
            return out == b'\n     1   2   3\n   +---+---+---+\na  |   |   |   |\n   +---+---+---+\nb  |   |   |   |\n   +---+---+---+\nc  |   |   |   |\n   +---+---+---+\n\nPlayer 1 select move (e.g. a2)>\n     1   2   3\n   +---+---+---+\na  | X |   |   |\n   +---+---+---+\nb  |   |   |   |\n   +---+---+---+\nc  |   |   |   |\n   +---+---+---+\n\nPlayer 2 select move (e.g. a2)>\n     1   2   3\n   +---+---+---+\na  | X |   |   |\n   +---+---+---+\nb  | O |   |   |\n   +---+---+---+\nc  |   |   |   |\n   +---+---+---+\n\nPlayer 1 select move (e.g. a2)>\n     1   2   3\n   +---+---+---+\na  | X |   |   |\n   +---+---+---+\nb  | O | X |   |\n   +---+---+---+\nc  |   |   |   |\n   +---+---+---+\n\nPlayer 2 select move (e.g. a2)>\n     1   2   3\n   +---+---+---+\na  | X |   |   |\n   +---+---+---+\nb  | O | X |   |\n   +---+---+---+\nc  |   | O |   |\n   +---+---+---+\n\nPlayer 1 select move (e.g. a2)>\n     1   2   3\n   +---+---+---+\na  | X |   |   |\n   +---+---+---+\nb  | O | X |   |\n   +---+---+---+\nc  |   | O | X |\n   +---+---+---+\n\nPlayer 1 has won!\nPlay again? (y/n)> \n'
        return Build("tests/tictactoe.c", 0, mars_callback=mars_callback)

    def array_complex_reduction_8():
        def mars_callback(p):
            out, err = p.communicate()
            return out == b"119462\n"
        return Build("tests/array_complex_reduction_8.c", 0, [MODE.GEN], mars_callback = mars_callback)

    builds = [
        fibonacci(),
        Build("tests/comments.c", [0, 0, 0, 240, 240]),
        tictactoe(),
        Build("tests/structs.c", 0),
        Build("tests/char.c", 0),
        Build("tests/while-loop-wrong.c", [245], [MODE.PARSER]),
        Build("tests/reserved-keywords.c", [245], [MODE.PARSER]),
        Build("tests/returning_multiple_times.c", 0),
        Build("tests/all-syntax.c", 0),
        Build("tests/same-fields-struct.c", [240], [MODE.SEM]),
        Build("tests/lvalue-violate.c", [240], [MODE.SEM]),
        Build("tests/arith-mismatch0.c", [240], [MODE.SEM]),
        Build("tests/arith-mismatch1.c", [240], [MODE.SEM]),
        Build("tests/returns.c", 0),
        Build("tests/unary-ops.c", 0),
        binary_search(),
        recur(),
        array_complex_reduction_8()
    ]

    succ_count = 0
    total_count = 0
    for build in builds:
        build.run()
        print(build)
        succ_count += build.succ_count
        total_count += build.count

    print("TOTAL: {}/{}".format(succ_count, total_count))

    if succ_count == total_count:
        exit(0)
    else:
        exit(-1)
