# kinoposter
Приложение для публикации описаний фильмов из БД в группе ВКонтакте.

## База данных
В БД содержатся описания фильмов (название, аннотация, актеры, ссылки на постер и кадры и так далее). 

## Алгоритм работы
1. Выбрать несколько случайных фильмов из БД
2. Сохранить изображения для каждого фильма
3. Получить для каждого фильма актуальный рейтинг с Кинопоиска
4. Составить список постов с датой и временем публикации – фильмы должны публиковаться в каждые 8, 14 и 20 часов (по московскому времени)
5. Создать отложенные посты в группе через API ВК

## Запуск
Приложение запускается по расписанию с требуемой периодичностью с использованием cron-а или Планировщика Windows.

## Конфигурация
В файле config.properties в директории программы задается строка подключения к БД, количество фильмов для публикации, id группы ВК, токен доступа ВК, id пользователя ВК.

## Технологии
Приложение разработано на Scala в функциональном стиле по концепции Tagless Final с использованием библиотек Cats и Cats Effect.
Доступ к БД с Doobie, чтение конфига с pureconfig, функциональное логгирование с log4cats.