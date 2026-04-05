package com.example.bulbulyator;

import android.content.Context;
import android.content.SharedPreferences;

public class SeedProducts {

    private static final String PREFS = "SeedPrefs";
    private static final String KEY   = "seeded_v5";

    public static void seedIfNeeded(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY, false)) return;

        SupabaseProductDao dao = SupabaseDb.getInstance().productDao();
        dao.deleteBySeller(0);

        Object[][] items = {
            {"iPhone 15 Pro",          "Смартфон Apple с чипом A17 Pro,",        99990, "https://picsum.photos/seed/iphone15/400/300",     "Электроника"},
            {"Samsung Galaxy S24",     "Флагманский Android-смартфон, 128 ГБ",           79990, "https://picsum.photos/seed/samsung24/400/300",    "Электроника"},
            {"Ноутбук ASUS ROG",       "Игровой ноутбук, RTX 4060, 16 ГБ RAM",           89990, "https://picsum.photos/seed/asusrog/400/300",      "Электроника"},

            // Автотовары
            {"Видеорегистратор 4K",    "Широкоугольная камера с ночным режимом",          4990, "https://picsum.photos/seed/dashcam4k/400/300",    "Автотовары"},
            {"Автомобильный пылесос",  "Беспроводной, 120 Вт, с насадками",               2490, "https://picsum.photos/seed/carvacuum/400/300",    "Автотовары"},
            {"Зимние шины R17",        "Комплект 4 шт, Michelin X-Ice",                  24990, "https://picsum.photos/seed/wintertyres/400/300",  "Автотовары"},

            // Услуги
            {"Ремонт телефонов",       "Замена экрана, батареи, разъёмов",                 990, "https://picsum.photos/seed/phonerepair/400/300",  "Услуги"},
            {"Уборка квартиры",        "Генеральная уборка, 3 часа, все материалы",       2500, "https://picsum.photos/seed/homeclean/400/300",    "Услуги"},
            {"Репетитор по математике","Подготовка к ЕГЭ, онлайн, 1 час",                1200, "https://picsum.photos/seed/mathtutor/400/300",    "Услуги"},

            // Игры
            {"PlayStation 5",          "Игровая консоль Sony, 825 ГБ SSD",               54990, "https://picsum.photos/seed/ps5300",   "Игры"},
            {"Xbox Series X",          "Игровая консоль Microsoft, 1 ТБ",                49990, "https://picsum.photos/seed/xboxserx/400/300",     "Игры"},
            {"Nintendo Switch OLED",   "Портативная консоль с OLED-экраном",             34990, "https://picsum.photos/seed/switcholed/400/300",   "Игры"},

            // Одежда
            {"Куртка зимняя мужская",  "Пуховик, водонепроницаемый, размер M-XXL",        7990, "https://picsum.photos/seed/winterjacket/400/300", "Одежда"},
            {"Платье летнее",          "Лёгкое хлопковое платье, разные цвета",           2490, "https://picsum.photos/seed/summerdress/400/300",  "Одежда"},
            {"Кроссовки Nike Air Max",  "Беговые кроссовки, размеры 36-46",               8990, "https://picsum.photos/seed/nikeairmax/400/300",   "Одежда"},

            // Спорт
            {"Гантели 10 кг пара",     "Разборные, с резиновым покрытием",                3490, "https://picsum.photos/seed/dumbbells10/400/300",  "Спорт"},
            {"Велосипед горный",       "21 скорость, алюминиевая рама, 26\"",             18990, "https://picsum.photos/seed/mtbbike/400/300",      "Спорт"},
            {"Коврик для йоги",        "Нескользящий, 6 мм, 183×61 см",                   1490, "https://picsum.photos/seed/yogamat2/400/300",     "Спорт"},

            // Детям
            {"Конструктор LEGO City",  "500 деталей, для детей от 6 лет",                 3990, "https://picsum.photos/seed/legocity/400/300",     "Детям"},
            {"Самокат детский",        "Трёхколёсный, до 20 кг, складной",                2990, "https://picsum.photos/seed/kidscooter/400/300",   "Детям"},
            {"Мягкая игрушка медведь", "Плюшевый, 50 см, гипоаллергенный",                 990, "https://picsum.photos/seed/teddybear/400/300",    "Детям"},

            // Для дома
            {"Умная колонка Яндекс",   "Яндекс Станция Мини с Алисой",                   4990, "https://picsum.photos/seed/smartspeaker/400/300", "Для дома"},
            {"Набор постельного белья", "Сатин, 2-спальный, 4 предмета",                  3490, "https://picsum.photos/seed/bedlinen/400/300",     "Для дома"},
            {"Светодиодная лента 5м",  "RGB, с пультом, 12В, IP65",                         890, "https://picsum.photos/seed/rgbstrip/400/300",     "Для дома"},

            // Красота
            {"Фен Dyson Supersonic",   "Профессиональный, 1600 Вт",                      29990, "https://picsum.photos/seed/dysonhair/400/300",    "Красота"},
            {"Набор кистей для макияжа","12 штук, натуральный ворс",                      1290, "https://picsum.photos/seed/makeupbrush/400/300",  "Красота"},
            {"Крем для лица SPF50",    "Увлажняющий, с защитой от UV",                    1890, "https://picsum.photos/seed/facecream/400/300",    "Красота"},

            // Здоровье
            {"Тонометр электронный",   "Автоматический, на запястье",                     2490, "https://picsum.photos/seed/bloodpress/400/300",   "Здоровье"},
            {"Витамины D3+K2",         "60 капсул, 2000 МЕ",                                890, "https://picsum.photos/seed/vitamind3/400/300",    "Здоровье"},
            {"Массажёр для шеи",       "Электрический, 6 режимов, с подогревом",          3490, "https://picsum.photos/seed/neckmassage/400/300",  "Здоровье"},

            // Продукты
            {"Мёд натуральный 1 кг",   "Цветочный, фермерский, без добавок",                690, "https://picsum.photos/seed/naturalhoney/400/300", "Продукты"},
            {"Кофе в зёрнах 1 кг",    "Арабика, обжарка средняя, Эфиопия",               1490, "https://picsum.photos/seed/arabicacoffee/400/300","Продукты"},
            {"Набор орехов",           "500 г, миндаль, кешью, изюм",                       890, "https://picsum.photos/seed/mixednuts/400/300",    "Продукты"},

            // Мебель
            {"Диван угловой",          "Раскладной, рогожка, серый",                     34990, "https://picsum.photos/seed/cornersofa/400/300",   "Мебель"},
            {"Письменный стол",        "120×60 см, МДФ, белый",                           8990, "https://picsum.photos/seed/writingdesk/400/300",  "Мебель"},
            {"Стеллаж книжный",        "5 полок, 180×80 см, дуб сонома",                  6490, "https://picsum.photos/seed/bookshelf/400/300",    "Мебель"},

            // Цветы
            {"Букет роз 25 шт",        "Красные розы, свежие, с упаковкой",               2990, "https://picsum.photos/seed/redroses/400/300",     "Цветы"},
            {"Орхидея в горшке",       "Фаленопсис, 2 ветки, белая",                      1490, "https://picsum.photos/seed/orcht/400/300",    "Цветы"},
            {"Суккуленты набор 5 шт",  "Разные виды, в керамических горшках",             1290, "https://picsum.photos/seed/succulentset/400/300", "Цветы"},

            // Товары для взрослых
            {"Вино красное сухое",     "Каберне Совиньон, Чили, 0.75 л",                    890, "https://picsum.photos/seed/redwine/400/300",      "Товары для взрослых"},
            {"Набор для покера",       "200 фишек, 2 колоды карт, кейс",                  2490, "https://picsum.photos/seed/pokerset/400/300",     "Товары для взрослых"},
            {"Виски Jack Daniel's",    "0.7 л, Tennessee Whiskey",                         2990, "https://picsum.photos/seed/jackdaniels/400/300",  "Товары для взрослых"},

            // Книги
            {"Мастер и Маргарита",     "Булгаков М.А., твёрдая обложка",                    590, "https://picsum.photos/seed/masterbook/400/300",   "Книги"},
            {"Атомные привычки",       "Джеймс Клир, бестселлер NYT",                       790, "https://picsum.photos/seed/atomicbook/400/300",   "Книги"},
            {"Гарри Поттер. Бокс",     "7 книг в подарочном боксе",                        4990, "https://picsum.photos/seed/harrypotter/400/300",  "Книги"},

            // Бытовая техника
            {"Робот-пылесос Xiaomi",   "S10+, лазерная навигация, 4000 Па",              24990, "https://picsum.photos/seed/robotvac/400/300",     "Бытовая техника"},
            {"Стиральная машина LG",   "7 кг, 1200 об/мин, инверторная",                 34990, "https://picsum.photos/seed/lgwasher/400/300",     "Бытовая техника"},
            {"Микроволновая печь",     "23 л, гриль, 800 Вт",                             8990, "https://picsum.photos/seed/microoven/400/300",    "Бытовая техника"},

            // Канцтовары
            {"Набор маркеров Copic",   "36 цветов, профессиональные",                     4990, "https://picsum.photos/seed/copicset/400/300",     "Канцтовары"},
            {"Ежедневник кожаный A5",  "Недатированный, 320 страниц",                     1490, "https://picsum.photos/seed/leatherplan/400/300",  "Канцтовары"},
            {"Ручка Parker Jotter",    "Шариковая, нержавеющая сталь",                    1990, "https://picsum.photos/seed/parkerjot/400/300",    "Канцтовары"},

            // Ювелирные изделия
            {"Кольцо с бриллиантом",   "585 проба, 0.15 карат",                          24990, "https://picsum.photos/seed/diamondring/400/300",  "Ювелирные изделия"},
            {"Серьги серебряные",      "925 проба, с фианитами",                           2490, "https://picsum.photos/seed/silverear/400/300",    "Ювелирные изделия"},
            {"Браслет золотой",        "14К, плетение панцирь, 18 см",                   12990, "https://picsum.photos/seed/goldbrace/400/300",    "Ювелирные изделия"},

            // Для ремонта
            {"Перфоратор Bosch",       "800 Вт, SDS-plus, 3 режима",                      8990, "https://picsum.photos/seed/boschdrill/400/300",   "Для ремонта"},
            {"Краска интерьерная",     "10 л, моющаяся, матовая, белая",                  2490, "https://picsum.photos/seed/wallpaint/400/300",    "Для ремонта"},
            {"Набор инструментов",     "108 предметов, кейс, ключи, биты",                3990, "https://picsum.photos/seed/toolset108/400/300",   "Для ремонта"},

            // Зоотовары
            {"Корм Royal Canin 15 кг", "Для взрослых собак средних пород",                5490, "https://picsum.photos/seed/royalcanin/400/300",   "Зоотовары"},
            {"Когтеточка для кошки",   "Напольная, сизаль, 80 см",                        1490, "https://picsum.photos/seed/catscratch/400/300",   "Зоотовары"},
            {"Аквариум 60 л",          "С крышкой, фильтром и подсветкой",                4990, "https://picsum.photos/seed/aquarium60/400/300",   "Зоотовары"},
        };

        for (Object[] item : items) {
            Product p = new Product();
            p.name        = (String) item[0];
            p.description = (String) item[1];
            p.price       = ((Number) item[2]).doubleValue();
            p.imageUrl    = (String) item[3];
            p.category    = (String) item[4];
            p.sellerId    = 0;
            p.sellerName  = "Магазин Bulbulyator";
            dao.insert(p);
        }

        prefs.edit().putBoolean(KEY, true).apply();
    }
}
