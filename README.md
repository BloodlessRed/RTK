
Написать консольную утилиту, которая должна посчитать статистику по всем файлам в указанном каталоге.
Значения, которые должна рассчитать и вывести в консоль утилита в разрезе расширения файлов:
1. Количество файлов
2. Размер в байтах
3. Количество строк всего
4. Количество не пустых строк (есть хотя бы один печатный символ)
5. Количество строк с комментариями (учитывать только однострочные комментарий в начале строки, реализовать как минимум для Java кода и Bash скриптов)

Параметры, которые должна принимать утилита:
1. <path> - путь до каталога по которому надо выполнить сбор статистики
2. --recursive - выполнять обход дерева рекурсивно
3. --max-depth=<number> - глубина рекурсивного обхода
4. --thread=<number> - количество потоков используемого для обхода
5. --include-ext=<ext1,ext2,ext3,..> - обрабатывать файлы только с указанными расширениями
6. --exclude-ext=<ext1,ext2,ext3,..> - не обрабатывать файлы с указанными расширениями
7. --git-ignore - не обрабатывать файлы указанные в файле .gitignore
8. --output=<plain,xml,json> - формат вывода статистики

Реализация ключей 7 и 8 по желанию.
Результат работы представить в виде ссылки на публичный репозиторий, например, github
