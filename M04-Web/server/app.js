const express = require('express');
const mysql = require('mysql2');
const path = require('path');
const hbs = require('hbs'); 

const app = express();
const PORT = 3000;

// ── CONFIGURACIÓN DE VISTAS ──────────────────────────
app.set('view engine', 'hbs');
app.set('views', path.join(__dirname, 'views'));
hbs.registerPartials(path.join(__dirname, 'views/partials'));

// ─── REGISTRO DE HELPERS PARA HANDLEBARS ──────────────

// 1. Formateo de números (1.000 en vez de 1000)
hbs.registerHelper('numberFormat', function(numero) {
    if (typeof numero !== 'number') return numero;
    return numero.toLocaleString('es-ES');
});

// 2. Valores por defecto
hbs.registerHelper('default', function(val, def) {
    return (val !== undefined && val !== null) ? val : def;
});

// 3. Barras de progreso
hbs.registerHelper('progress', function(valor, multiplicador) {
    const total = Math.min((valor || 0) * multiplicador, 100);
    return total + '%';
});

// 4. Multiplicar
hbs.registerHelper('multiply', function(a, b) {
    return (a || 0) * b;
});

// 5. Concatenar textos
hbs.registerHelper('concat', function() {
    let out = "";
    for (let i = 0; i < arguments.length - 1; i++) {
        out += arguments[i];
    }
    return out;
});

// 6. Comprobar si el ejército está vacío (Soporta múltiples argumentos)
hbs.registerHelper('isEmptyArmy', function (...args) {
    const armies = args.slice(0, -1); 
    return armies.every(army => !army || army.length === 0);
});

// 7. Comparación de igualdad
hbs.registerHelper('eq', function(a, b) {
    return a === b;
});

// 8. Clase CSS para el Log de batalla
hbs.registerHelper('getLogClass', function(logEntry) {
    if (!logEntry) return 'log-neutral';
    const entry = logEntry.toLowerCase();
    if (entry.includes('victoria') || entry.includes('ganado')) return 'log-success';
    if (entry.includes('derrota') || entry.includes('perdido')) return 'log-danger';
    return 'log-neutral';
});

// Archivos estáticos
app.use(express.static(path.join(__dirname, '../public')));

// ─── CONEXIÓN A LA BASE DE DATOS ───────────────────────────────────────────
const db = mysql.createConnection({
    host: 'localhost',
    user: 'root',         
    password: '1234.',     
    database: 'civilizations_db'
});

db.connect((err) => {
    if (err) console.error('Error al conectar con la BD:', err.message);
    else console.log('Conexión a MySQL OK');
});

// ─── RUTAS ───────────────────────────────────────

app.get('/', (req, res) => {
    const sqlBatallas = `
        SELECT bs.num_battle, bs.wood_acquired, bs.iron_acquired,
               SUM(cas.drops) AS civilization_drops,
               SUM(eas.drops) AS enemy_drops
        FROM battle_stats bs
        LEFT JOIN civilization_attack_stats cas ON bs.num_battle = cas.num_battle AND bs.civilization_id = cas.civilization_id
        LEFT JOIN enemy_attack_stats eas ON bs.num_battle = eas.num_battle AND bs.civilization_id = eas.civilization_id
        WHERE bs.civilization_id = 1
        GROUP BY bs.num_battle, bs.wood_acquired, bs.iron_acquired
        ORDER BY bs.num_battle DESC
        LIMIT 2
    `;
    db.query(sqlBatallas, (err, batallas) => {
        res.render('index', { batallas: err ? [] : batallas });
    });
});

app.get('/batallas', (req, res) => {
    const sqlTotal = `SELECT COUNT(*) AS total FROM battle_stats WHERE civilization_id = 1`;
    const sqlBatallas = `
        SELECT bs.num_battle, bs.wood_acquired, bs.iron_acquired,
               (SELECT SUM(cas2.initial_count) FROM civilization_attack_stats cas2 WHERE cas2.num_battle = bs.num_battle AND cas2.civilization_id = bs.civilization_id) AS civUnidades,
               (SELECT SUM(eas2.initial_count) FROM enemy_attack_stats eas2 WHERE eas2.num_battle = bs.num_battle AND eas2.civilization_id = bs.civilization_id) AS enemUnidades
        FROM battle_stats bs
        WHERE bs.civilization_id = 1
        ORDER BY bs.num_battle DESC
    `;
    db.query(sqlTotal, (err, totalResult) => {
        const total = (err || !totalResult.length) ? 0 : totalResult[0].total;
        db.query(sqlBatallas, (err2, batallas) => {
            res.render('batallas', { batallas: err2 ? [] : batallas, total: total });
        });
    });
});

app.get('/informe', (req, res) => {
    const id = parseInt(req.query.id) || 1;
    const sqlBatalla = `SELECT * FROM battle_stats WHERE num_battle = ? AND civilization_id = 1`;
    const sqlCivAtaque = `SELECT type, initial_count, drops FROM civilization_attack_stats WHERE num_battle = ? AND civilization_id = 1`;
    const sqlCivDefensa = `SELECT type, initial_count, drops FROM civilization_defense_stats WHERE num_battle = ? AND civilization_id = 1`;
    const sqlCivEspecial = `SELECT type, initial_count, drops FROM civilization_special_stats WHERE num_battle = ? AND civilization_id = 1`;
    const sqlEnemy = `SELECT type, initial_count, drops FROM enemy_attack_stats WHERE num_battle = ? AND civilization_id = 1`;
    const sqlLog = `SELECT log_entry FROM battle_log WHERE num_battle = ? AND civilization_id = 1 ORDER BY num_line ASC`;

    db.query(sqlBatalla, [id], (err, batallaResult) => {
        const batalla = (err || !batallaResult.length) ? null : batallaResult[0];
        db.query(sqlCivAtaque, [id], (err2, civAtaque) => {
            db.query(sqlCivDefensa, [id], (err3, civDefensa) => {
                db.query(sqlCivEspecial, [id], (err4, civEspecial) => {
                    db.query(sqlEnemy, [id], (err5, enemy) => {
                        db.query(sqlLog, [id], (err6, log) => {
                            res.render('informe', {
                                id: id, batalla: batalla,
                                civAtaque: civAtaque || [],
                                civDefensa: civDefensa || [],
                                civEspecial: civEspecial || [],
                                enemy: enemy || [],
                                log: log || []
                            });
                        });
                    });
                });
            });
        });
    });
});

app.get('/civilizacion', (req, res) => {
    const sqlCiv = `SELECT * FROM civilization_stats WHERE civilization_id = 1`;
    db.query(sqlCiv, (err, result) => {
        res.render('civilizacion', { civ: (err || !result.length) ? null : result[0] });
    });
});

app.get('/programadores', (req, res) => {
    const programadores = [
        { nombre: 'Valeria', rol: 'Programador Java',foto: "img/knight.png", tareas: ['Clase Civilization y excepciones', 'Clases de unidades de ataque', 'Interface MilitaryUnit y Variables'] },
        { nombre: 'Miguel', rol: 'Programador Java',foto: "img/miguel.png",tareas: ['Clase Battle', 'Clases de unidades defensivas y especiales', 'Clase Main y TimerTask'] },
        { nombre: 'Diego', rol: 'Ptogramador Java',foto: "img/soldier.png", tareas: ['Script SQL y DAOs', 'Servidor Node.js y páginas HBS', 'CSS y diseño responsive'] }
    ];
    res.render('programadores', { programadores });
});

app.listen(PORT, () => console.log('Servidor arrancado en http://localhost:' + PORT));