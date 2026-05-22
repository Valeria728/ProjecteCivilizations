const express = require('express');
const mysql = require('mysql2');
const path = require('path');
const hbs = require('hbs'); 
const MySQL = require('./utilsMySQL');

const app = express();
const port = 3000;

// ─── CONEXIÓN A LA BASE DE DATOS ───────────────────────────────────────────
const isProxmox = !!process.env.PM2_HOME;

const db = new MySQL();
if (!isProxmox) {
  db.init({
    host: '127.0.0.1',
    port: 3307,
    user: 'miguel',
    password: 'civilizacion.',
    database: 'civilizations_db'
  });
} else {
  db.init({
    host: '127.0.0.1',
    port: 3306,
    user: 'miguel',
    password: 'civilizacion.',
    database: 'civilizations_db'
  });
}

// Archivos estáticos y procesado de formularios
app.use(express.static('public'));
app.use(express.urlencoded({ extended: true }));

// Desactivar caché
app.use((req, res, next) => {
  res.setHeader('Cache-Control', 'no-store, no-cache, must-revalidate, proxy-revalidate');
  res.setHeader('Pragma', 'no-cache');
  res.setHeader('Expires', '0');
  res.setHeader('Surrogate-Control', 'no-store');
  next();
});

// ── CONFIGURACIÓN DE VISTAS ──────────────────────────
app.set('view engine', 'hbs');
app.set('views', path.join(__dirname, 'views'));
hbs.registerPartials(path.join(__dirname, 'views/partials'));

// ─── REGISTRO DE HELPERS PARA HANDLEBARS ──────────────
hbs.registerHelper('numberFormat', function(numero) {
    if (typeof numero !== 'number') return numero;
    return numero.toLocaleString('es-ES');
});

hbs.registerHelper('default', function(val, def) {
    return (val !== undefined && val !== null) ? val : def;
});

hbs.registerHelper('progress', function(valor, multiplicador) {
    const total = Math.min((valor || 0) * multiplicador, 100);
    return total + '%';
});

hbs.registerHelper('multiply', function(a, b) {
    return (a || 0) * b;
});

hbs.registerHelper('concat', function() {
    let out = "";
    for (let i = 0; i < arguments.length - 1; i++) {
        out += arguments[i];
    }
    return out;
});

hbs.registerHelper('isEmptyArmy', function (...args) {
    const armies = args.slice(0, -1); 
    return armies.every(army => !army || army.length === 0);
});

hbs.registerHelper('eq', function(a, b) {
    return a === b;
});

hbs.registerHelper('getLogClass', function(logEntry) {
    if (!logEntry) return 'log-neutral';
    const entry = logEntry.toLowerCase();
    if (entry.includes('victoria') || entry.includes('ganado')) return 'log-success';
    if (entry.includes('derrota') || entry.includes('perdido')) return 'log-danger';
    return 'log-neutral';
});

// Archivos estáticos
app.use(express.static(path.join(__dirname, '../public')));


// ─── RUTAS ───────────────────────────────────────
// ─── RUTAS CORREGIDAS Y DESANIDADAS ───────────────────────────────────────


// 1. Ruta Principal (Corregida para usar Callbacks tradicionales como el resto)
app.get('/', async (req, res) => {
    try {
        // Obtenir les dades de la base de dades
        const rows = await db.query(`
        SELECT bs.num_battle as num_battle, 
        bs.wood_acquired as wood_acquired, 
        bs.iron_acquired as iron_acquired,
        SUM(cas.drops) AS civilization_drops,
        SUM(eas.drops) AS enemy_drops
        FROM battle_stats bs
        LEFT JOIN civilization_attack_stats cas ON bs.num_battle = cas.num_battle AND bs.civilization_id = cas.civilization_id
        LEFT JOIN enemy_attack_stats eas ON bs.num_battle = eas.num_battle AND bs.civilization_id = eas.civilization_id
        WHERE bs.civilization_id = 1
        GROUP BY bs.num_battle, bs.wood_acquired, bs.iron_acquired
        ORDER BY bs.num_battle DESC
        LIMIT 2
        `);

        const data = db.table_to_json(rows, { 
         num_battle: 'number', 
         wood_acquired: 'number',
         iron_acquired: 'number',
         civilization_drops: 'number',
         enemy_drops: 'number'
        });

        // Renderitzar la plantilla amb les dades
        res.render('index', data);
    } catch (err) {
        console.error(err);
        res.status(500).send('Error consultant la base de dades');
    }
    
});

// 2. Ruta de Batallas
app.get('/batallas', async (req, res) => {
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


// 3. Ruta de Informe (¡DESANIDADA usando funciones independientes!)
app.get('/informe', async (req, res) => {
    const id = parseInt(req.query.id) || 1;

    const sqlBatalla = `SELECT * FROM battle_stats WHERE num_battle = ? AND civilization_id = 1`;
    const sqlCivAtaque = `SELECT type, initial_count, drops FROM civilization_attack_stats WHERE num_battle = ? AND civilization_id = 1`;
    const sqlCivDefensa = `SELECT type, initial_count, drops FROM civilization_defense_stats WHERE num_battle = ? AND civilization_id = 1`;
    const sqlCivEspecial = `SELECT type, initial_count, drops FROM civilization_special_stats WHERE num_battle = ? AND civilization_id = 1`;
    const sqlEnemy = `SELECT type, initial_count, drops FROM enemy_attack_stats WHERE num_battle = ? AND civilization_id = 1`;
    const sqlLog = `SELECT log_entry FROM battle_log WHERE num_battle = ? AND civilization_id = 1 ORDER BY num_line ASC`;


    // Objeto temporal donde iremos guardando los resultados de cada consulta
    let datosInforme = {
        id: id,
        batalla: null,
        civAtaque: [],
        civDefensa: [],
        civEspecial: [],
        enemy: [],
        log: []
    };

    // Definimos las funciones encargadas de cada paso de forma plana
    function cargarBatalla() {
        const sql = `SELECT * FROM battle_stats WHERE num_battle = ? AND civilization_id = 1`;
        db.query(sql, [id], (err, result) => {
            datosInforme.batalla = (err || !result.length) ? null : result[0];
            cargarCivAtaque(); // Saltamos al siguiente paso
        });
    }

    function cargarCivAtaque() {
        const sql = `SELECT type, initial, drops FROM civilization_attack_stats WHERE num_battle = ? AND civilization_id = 1`;
        db.query(sql, [id], (err, result) => {
            datosInforme.civAtaque = result || [];
            cargarCivDefensa();
        });
    }

    function cargarCivDefensa() {
        const sql = `SELECT type, initial, drops FROM civilization_defense_stats WHERE num_battle = ? AND civilization_id = 1`;
        db.query(sql, [id], (err, result) => {
            datosInforme.civDefensa = result || [];
            cargarCivEspecial();
        });
    }

    function cargarCivEspecial() {
        const sql = `SELECT type, initial, drops FROM civilization_special_stats WHERE num_battle = ? AND civilization_id = 1`;
        db.query(sql, [id], (err, result) => {
            datosInforme.civEspecial = result || [];
            cargarEnemy();
        });
    }

    function cargarEnemy() {
        const sql = `SELECT type, initial, drops FROM enemy_attack_stats WHERE num_battle = ? AND civilization_id = 1`;
        db.query(sql, [id], (err, result) => {
            datosInforme.enemy = result || [];
            cargarLog();
        });
    }

    function cargarLog() {
        const sql = `SELECT log_entry FROM battle_log WHERE num_battle = ? AND civilization_id = 1 ORDER BY num_line ASC`;
        db.query(sql, [id], (err, result) => {
            datosInforme.log = result || [];
            
            // ¡Único render al final de todo el flujo plano!
            res.render('informe', datosInforme);
        });
    }

    // Arrancamos la cadena de funciones independientes
    cargarBatalla();
});


// 4. Ruta de Civilización
app.get('/civilizacion', async (req, res) => {
    const sqlCiv = `SELECT * FROM civilization_stats WHERE civilization_id = 1`;
    db.query(sqlCiv, (err, result) => {
        res.render('civilizacion', { civ: (err || !result.length) ? null : result[0] });
    });
});

// 5. Ruta de Programadores
app.get('/programadores', async (req, res) => {
    const programadores = [
        { nombre: 'Valeria', rol: 'Programador Java', foto: "img/knight.png", tareas: ['Clase Civilization y excepciones', 'Clases de unidades de ataque', 'Interface MilitaryUnit y Variables'] },
        { nombre: 'Miguel', rol: 'Programador Java', foto: "img/miguel.png", tareas: ['Clase Battle', 'Clases de unidades defensivas y especiales', 'Clase Main y TimerTask'] },
        { nombre: 'Diego', rol: 'Programador Java', foto: "img/soldier.png", tareas: ['Script SQL y DAOs', 'Servidor Node.js y páginas HBS', 'CSS y diseño responsive'] }
    ];
    res.render('programadores', { programadores });
});

// Inicio del servidor
// Start server
const httpServer = app.listen(port, () => {
  console.log(`http://localhost:${port}`);
});

// Graceful shutdown
process.on('SIGINT', async () => {
  await db.end();
  httpServer.close();
  process.exit(0);
});