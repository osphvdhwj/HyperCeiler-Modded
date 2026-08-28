import os

def fix_imports(directory):
    for root, dirs, files in os.walk(directory):
        for file in files:
            if not file.endswith(".kt") and not file.endswith(".java"):
                continue
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()

            needs_write = False
            if 'loadClassOrNull' in content and 'import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClassOrNull' not in content:
                lines = content.split('\n')
                for i, line in enumerate(lines):
                    if line.startswith('package '):
                        lines.insert(i + 1, 'import io.github.kyuubiran.ezxhelper.core.util.ClassUtil.loadClassOrNull')
                        break
                content = '\n'.join(lines)
                needs_write = True

            if '.methodFinder(' in content and 'import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder' not in content:
                lines = content.split('\n')
                for i, line in enumerate(lines):
                    if line.startswith('package '):
                        lines.insert(i + 1, 'import io.github.kyuubiran.ezxhelper.core.finder.MethodFinder.`-Static`.methodFinder')
                        break
                content = '\n'.join(lines)
                needs_write = True

            if needs_write:
                with open(path, 'w') as f:
                    f.write(content)
                print(f"Fixed {path}")

fix_imports("library/hook/src/main/java")
