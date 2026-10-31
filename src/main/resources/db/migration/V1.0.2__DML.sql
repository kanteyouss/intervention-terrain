-- =========================================================================
-- V1.0.2__DML.sql : JEU DE DONNÉES DE RÉFÉRENCE ET DE TEST (JDD)
-- PROJET 3 — LES INTERVENTIONS TERRAIN
-- =========================================================================

-- =========================================================================
-- 1. NETTOYAGE PRÉALABLE DES TABLES
-- =========================================================================
TRUNCATE TABLE
    alerte_competence,
    historique_statut,
    intervention_piece,
    stock,
    piece,
    depot,
    intervention_technicien,
    intervention_creneau,
    intervention,
    statut,
    client,
    creneau,
    type_intervention_competence,
    types_intervention,
    technicien_competence,
    competence,
    technicien,
    equipe,
    agence,
    commune,
    departement,
    region
RESTART IDENTITY CASCADE;

-- =========================================================================
-- 2. RÉFÉRENTIELS GÉOGRAPHIQUES (Régions, Départements, Communes)
-- =========================================================================
INSERT INTO region (id, nom) VALUES
    (1, 'Abidjan'),
    (2, 'Yamoussoukro'),
    (3, 'Gbêkê'),
    (4, 'Bas-Sassandra'),
    (5, 'Savanes');

INSERT INTO departement (id, nom, code, region_id) VALUES
    (1, 'Abidjan', 'ABJ', 1),
    (2, 'Bingerville', 'BGV', 1),
    (3, 'Yamoussoukro', 'YAM', 2),
    (4, 'Bouaké', 'BKE', 3),
    (5, 'San-Pédro', 'SPY', 4),
    (6, 'Korhogo', 'KGO', 5);

INSERT INTO commune (id, nom, code_postal, departement_id) VALUES
    (1, 'Cocody', '08 BP 1', 1),
    (2, 'Plateau', '01 BP 1', 1),
    (3, 'Yopougon', '19 BP 1', 1),
    (4, 'Marcory', '05 BP 1', 1),
    (5, 'Bingerville Centre', 'BG 1', 2),
    (6, 'Yamoussoukro Centre', '01 BP YAM', 3),
    (7, 'Bouaké Commerce', '01 BP BKE', 4),
    (8, 'San-Pédro Cité', '01 BP SPY', 5),
    (9, 'Korhogo Ville', '01 BP KGO', 6);

-- =========================================================================
-- 3. AGENCES ET ÉQUIPES
-- =========================================================================
INSERT INTO agence (id, nom, ville, responsable, commune_id) VALUES
    (1, 'Agence Abidjan Nord', 'Abidjan', 'Kouassi Jean', 1),
    (2, 'Agence Plateau Centre', 'Abidjan', 'Yao Aya', 2),
    (3, 'Agence Yopougon Ouest', 'Abidjan', 'Bamba Souleymane', 3),
    (4, 'Agence Yamoussoukro', 'Yamoussoukro', 'Doumbia Mamadou', 6),
    (5, 'Agence Bouaké Région', 'Bouaké', 'Gnahoré Éric', 7);

INSERT INTO equipe (id, nom, agence_id) VALUES
    (1, 'Équipe Fibre & Télécom', 1),
    (2, 'Équipe Électrotechnique', 1),
    (3, 'Équipe Maintenance Centre', 2),
    (4, 'Équipe Déploiement Ouest', 3),
    (5, 'Équipe Intervention Intérieur', 4);

-- =========================================================================
-- 4. STATUTS ET CRÉNEAUX HORAIRES
-- =========================================================================
INSERT INTO statut (id, libelle) VALUES
    (1, 'PLANIFIEE'),
    (2, 'EN_COURS'),
    (3, 'TERMINEE'),
    (4, 'ANNULEE'),
    (5, 'REPORTEE'),
    (6, 'ECHOUEE');

INSERT INTO creneau (id, date_debut, date_fin) VALUES
    (1, '08:00:00', '10:00:00'),
    (2, '10:00:00', '12:00:00'),
    (3, '13:00:00', '15:00:00'),
    (4, '15:00:00', '17:00:00'),
    (5, '08:30:00', '11:30:00'),
    (6, '14:00:00', '18:00:00');

-- =========================================================================
-- 5. COMPÉTENCES ET TYPES D'INTERVENTION
-- =========================================================================
INSERT INTO competence (id, libelle, description) VALUES
    (1, 'Raccordement Fibre Optique', 'Soudure et tirage de câble fibre FTTH/FTTO'),
    (2, 'Habilitation Électrique BT', 'Intervention basse tension NF C 18-510'),
    (3, 'Configuration Routeurs & Switchs', 'Paramétrage des équipements réseaux IP Cisco et Huawei'),
    (4, 'Plomberie & Chauffage Industriel', 'Pose et dépannage circuits hydrauliques'),
    (5, 'Sécurité Incendie & SSI', 'Maintenance des déclencheurs et centrales'),
    (6, 'Câblage Structuré VDI', 'Installation baie de brassage et prises RJ45'),
    (7, 'Froid et Climatisation', 'Charge de fluide frigorigène et compresseur'),
    (15, 'Audit Énergétique & Bilan Carbone', 'Mesure et certification efficacité thermique');

INSERT INTO types_intervention (id, nom, description, delai_heures) VALUES
    (1, 'Raccordement Fibre', 'Raccordement abonné au réseau fibre optique', 2),
    (2, 'Maintenance Électrique', 'Vérification armoires et sécurité électrique', 4),
    (3, 'Installation Baie Réseau', 'Mise en place de switch, routeur et baie VDI', 4),
    (4, 'Dépannage Urgent Climatisation', 'Intervention express sur groupe de froid', 3),
    (5, 'Audit Conformité SSI', 'Contrôle réglementaire des systèmes de sécurité', 2);

-- Exigences de compétences par type d'intervention (F13 & F17)
INSERT INTO type_intervention_competence (type_intervention_id, competence_id) VALUES
    (1, 1), -- Raccordement Fibre exige: Raccordement Fibre Optique
    (2, 2), -- Maintenance Électrique exige: Habilitation Électrique BT
    (3, 3), -- Installation Baie Réseau exige: Configuration Routeurs & Switchs
    (3, 6), -- Installation Baie Réseau exige aussi: Câblage Structuré VDI
    (4, 7), -- Dépannage Climatisation exige: Froid et Climatisation
    (5, 5); -- Audit SSI exige: Sécurité Incendie & SSI

-- =========================================================================
-- 6. TECHNICIENS ET COMPÉTENCES (F6, F7, F16)
-- =========================================================================
INSERT INTO technicien (id, nom, prenom, email, telephone, matricule, equipe_id) VALUES
    (1, 'Koffi', 'Serge', 'serge.koffi@testo.ci', '+2250701020304', 'TECH-001', 1),
    (2, 'Diallo', 'Moussa', 'moussa.diallo@testo.ci', '+2250702030405', 'TECH-002', 1),
    (3, 'Amani', 'Christian', 'christian.amani@testo.ci', '+2250703040506', 'TECH-003', 1),
    (4, 'Bakayoko', 'Aminata', 'aminata.bakayoko@testo.ci', '+2250704050607', 'TECH-004', 2),
    (5, 'Konan', 'Yves', 'yves.konan@testo.ci', '+2250705060708', 'TECH-005', 2),
    (6, 'Coulibaly', 'Ibrahim', 'ibrahim.coulibaly@testo.ci', '+2250706070809', 'TECH-006', 3),
    (7, 'Traoré', 'Fatou', 'fatou.traore@testo.ci', '+2250707080910', 'TECH-007', 3),
    (8, 'Ouattara', 'Alassane', 'alassane.ouattara@testo.ci', '+2250708091011', 'TECH-008', 4),
    (9, 'N''Dri', 'Ange', 'ange.ndri@testo.ci', '+2250709101112', 'TECH-009', 4),
    (10, 'Yapi', 'Gilles', 'gilles.yapi@testo.ci', '+2250710111213', 'TECH-010', 5);

-- Techniciens compétences (avec dates pour tester F7 et alertes F16)
INSERT INTO technicien_competence (id, technicien_id, competence_id, date_obtention, date_expiration) VALUES
    -- Tech 1 (Koffi Serge) : Fibre valide, Élec valide
    (1, 1, 1, CURRENT_DATE - INTERVAL '1 year', CURRENT_DATE + INTERVAL '1 year'),
    (2, 1, 2, CURRENT_DATE - INTERVAL '6 months', CURRENT_DATE + INTERVAL '18 months'),
    -- Tech 2 (Diallo Moussa) : Fibre valide, Réseau expire dans 15 jours (alerte F16!)
    (3, 2, 1, CURRENT_DATE - INTERVAL '2 years', CURRENT_DATE + INTERVAL '6 months'),
    (4, 2, 3, CURRENT_DATE - INTERVAL '1 year', CURRENT_DATE + INTERVAL '15 days'),
    -- Tech 3 (Amani Christian) : Compétence EXPIRÉE pour tester F7 / F13
    (5, 3, 1, CURRENT_DATE - INTERVAL '3 years', CURRENT_DATE - INTERVAL '10 days'),
    -- Tech 4 (Bakayoko Aminata) : Électricité BT valide, SSI valide
    (6, 4, 2, CURRENT_DATE - INTERVAL '8 months', CURRENT_DATE + INTERVAL '2 years'),
    (7, 4, 5, CURRENT_DATE - INTERVAL '1 year', CURRENT_DATE + INTERVAL '1 year'),
    -- Tech 5 (Konan Yves) : Climatisation valide, Élec expire dans 20 jours (alerte F16)
    (8, 5, 7, CURRENT_DATE - INTERVAL '1 year', CURRENT_DATE + INTERVAL '1 year'),
    (9, 5, 2, CURRENT_DATE - INTERVAL '2 years', CURRENT_DATE + INTERVAL '20 days'),
    -- Tech 6 (Coulibaly Ibrahim) : Configuration Réseaux & Câblage VDI valides
    (10, 6, 3, CURRENT_DATE - INTERVAL '6 months', CURRENT_DATE + INTERVAL '2 years'),
    (11, 6, 6, CURRENT_DATE - INTERVAL '6 months', CURRENT_DATE + INTERVAL '2 years'),
    -- Tech 7 (Traoré Fatou) : Sans expiration (valide indéfiniment)
    (12, 7, 1, CURRENT_DATE - INTERVAL '1 year', NULL),
    -- Tech 8 (Ouattara Alassane) : Climatisation valide
    (13, 8, 7, CURRENT_DATE - INTERVAL '5 months', CURRENT_DATE + INTERVAL '1 year'),
    -- Tech 9 (N'Dri Ange) : Électricité BT valide
    (14, 9, 2, CURRENT_DATE - INTERVAL '4 months', CURRENT_DATE + INTERVAL '2 years'),
    -- Tech 10 (Yapi Gilles) : Habilitation BT et Fibre
    (15, 10, 1, CURRENT_DATE - INTERVAL '7 months', CURRENT_DATE + INTERVAL '1 year'),
    (16, 10, 2, CURRENT_DATE - INTERVAL '7 months', CURRENT_DATE + INTERVAL '1 year');

-- =========================================================================
-- 7. CLIENTS (F8)
-- =========================================================================
INSERT INTO client (id, nom, prenom, email, telephone, adresse, commune) VALUES
    (1, 'Brou', 'Clarisse', 'clarisse.brou@email.com', '+2250102030405', 'Rue des Jardins, Villa 12', 'Cocody'),
    (2, 'Koné', 'Lassina', 'lassina.kone@email.com', '+2250203040506', 'Avenue Chardy, Imm. Trade', 'Plateau'),
    (3, 'Touré', 'Sékou', 'sekou.toure@email.com', '+2250304050607', 'Boulevard Principal, Sicogi', 'Yopougon'),
    (4, 'Soro', 'Guillaume', 'guillaume.soro@email.com', '+2250405060708', 'Zone Industrielle Vridi', 'Marcory'),
    (5, 'Diabaté', 'Mariam', 'mariam.diabate@email.com', '+2250506070809', 'Quartier Résidentiel', 'Yamoussoukro'),
    (6, 'Assi', 'Fabrice', 'fabrice.assi@email.com', '+2250607080910', 'Quartier Air France', 'Bouaké');

-- =========================================================================
-- 8. INTERVENTIONS, CRÉNEAUX ET HISTORIQUE (F8, F9, F10, F12, F14, F15, F18, F19)
-- =========================================================================

-- Interventions du mois en cours (CURRENT_DATE)
INSERT INTO intervention (id, date_planifiee, statut, commentaire, reference, type_intervention_id, client_id) VALUES
    -- Aujourd'hui : Interventions pour calcul charge / restitution F17 / F18
    (1, CURRENT_DATE, 'EN_COURS', 'Raccordement fibre duplex salon', 'INT-2026-001', 1, 1),
    (2, CURRENT_DATE + INTERVAL '10 days', 'PLANIFIEE', 'Contrôle tableau divisionnaire', 'INT-2026-002', 2, 2),
    (3, CURRENT_DATE, 'ANNULEE', 'Client absent au rendez-vous', 'INT-2026-003', 1, 3),
    -- Intervention PLANIFIEE pour tester transition vers ANNULEE (F14)
    (4, CURRENT_DATE + INTERVAL '8 days', 'PLANIFIEE', 'Raccordement FTTH à planifier', 'INT-2026-004', 1, 1),
    -- Intervention EN_COURS pour tester transition vers TERMINEE (F14)
    (5, CURRENT_DATE - INTERVAL '1 day', 'EN_COURS', 'Maintenance armoire TGBT en cours', 'INT-2026-005', 2, 4),
    -- Intervention PLANIFIEE pour tester CRUD F5 (Consultation, Reprogrammation, Suppression)
    (6, CURRENT_DATE + INTERVAL '5 days', 'PLANIFIEE', 'Installation baie réseau prévue', 'INT-2026-006', 3, 2),
    (7, CURRENT_DATE - INTERVAL '6 days', 'TERMINEE', 'Installation baie réseau complète', 'INT-2026-007', 3, 2),
    -- Intervention EN_COURS pour tester transition vers ECHOUEE (F14)
    (8, CURRENT_DATE - INTERVAL '1 day', 'EN_COURS', 'Compresseur dépannage en cours', 'INT-2026-008', 4, 5),
    (9, CURRENT_DATE - INTERVAL '10 days', 'TERMINEE', 'Audit de sécurité incendie validé sans réserve', 'INT-2026-009', 5, 4),
    (10, CURRENT_DATE - INTERVAL '12 days', 'TERMINEE', 'Raccordement fibre entreprise', 'INT-2026-010', 1, 2),
    -- Interventions futures
    (11, CURRENT_DATE + INTERVAL '2 days', 'PLANIFIEE', 'Maintenance planifiée serveurs et baie', 'INT-2026-011', 3, 2),
    (12, CURRENT_DATE + INTERVAL '3 days', 'PLANIFIEE', 'Raccordement nouvel abonné fibre', 'INT-2026-012', 1, 5),
    -- Mois dernier pour statistiques comparatives (F19)
    (13, CURRENT_DATE - INTERVAL '1 month', 'TERMINEE', 'Installation initiale fibre', 'INT-2026-013', 1, 1),
    (14, CURRENT_DATE - INTERVAL '1 month', 'TERMINEE', 'Vérification disjoncteurs', 'INT-2026-014', 2, 4),
    (15, CURRENT_DATE - INTERVAL '1 month', 'ECHOUEE', 'Absence d alimentation générale sur site', 'INT-2026-015', 2, 3),
    -- Intervention ECHOUEE pour tester la reprise F15
    (16, CURRENT_DATE - INTERVAL '2 days', 'ECHOUEE', 'Compresseur hors service - échec à reprendre', 'INT-2026-016', 4, 5);

-- Association Intervention - Créneau
INSERT INTO intervention_creneau (id, creneau_id, intervention_id) VALUES
    (1, 1, 1),   -- INT 1 sur créneau 08:00 - 10:00 (2h)
    (2, 2, 2),   -- INT 2 sur créneau 10:00 - 12:00 (2h)
    (3, 3, 3),   -- INT 3 sur créneau 13:00 - 15:00
    (4, 1, 4),   -- INT 4 sur créneau 08:00 - 10:00
    (5, 6, 5),   -- INT 5 sur créneau 14:00 - 18:00 (4h)
    (6, 4, 6),   -- INT 6 sur créneau 15:00 - 17:00 (2h)
    (7, 6, 7),   -- INT 7 sur créneau 14:00 - 18:00 (4h)
    (8, 5, 8),   -- INT 8 sur créneau 08:30 - 11:30 (3h)
    (9, 1, 9),   -- INT 9 sur créneau 08:00 - 10:00 (2h)
    (10, 2, 10), -- INT 10 sur créneau 10:00 - 12:00 (2h)
    (11, 3, 11), -- INT 11 sur créneau 13:00 - 15:00 (2h)
    (12, 1, 12), -- INT 12 sur créneau 08:00 - 10:00 (2h)
    (13, 1, 13),
    (14, 2, 14),
    (15, 3, 15),
    (16, 5, 16);

-- Association Intervention - Technicien
INSERT INTO intervention_technicien (id, est_responsable, intervention_id, technicien_id) VALUES
    (1, true, 1, 1),   -- Tech 1 (Koffi Serge)
    (2, true, 2, 4),   -- Tech 4 (Bakayoko Aminata)
    (3, true, 3, 2),   -- Tech 2 (Diallo Moussa)
    (4, true, 4, 1),   -- Tech 1
    (5, true, 5, 4),   -- Tech 4
    (6, true, 6, 6),   -- Tech 6 (Coulibaly Ibrahim)
    (7, true, 7, 6),   -- Tech 6
    (8, true, 8, 8),   -- Tech 8 (Ouattara Alassane)
    (9, true, 9, 4),   -- Tech 4
    (10, true, 10, 1), -- Tech 1
    (11, true, 11, 6), -- Tech 6
    (12, true, 12, 10),-- Tech 10
    (13, true, 13, 1),
    (14, true, 14, 4),
    (15, true, 15, 4),
    (16, true, 16, 8);

-- Historique des changements de statut (F14)
INSERT INTO historique_statut (id, date_changement, auteur, intervention_id, statut_id) VALUES
    (1, CURRENT_DATE - INTERVAL '1 day' + TIME '08:00:00', 'superviseur_abj', 1, 1),
    (2, CURRENT_DATE + TIME '08:30:00', 'tech_koffi', 1, 2),
    (3, CURRENT_DATE + TIME '09:00:00', 'superviseur_abj', 2, 1),
    (4, CURRENT_DATE - INTERVAL '5 days' + TIME '08:00:00', 'superviseur_abj', 4, 1),
    (5, CURRENT_DATE - INTERVAL '2 days' + TIME '09:45:00', 'tech_koffi', 4, 3),
    (6, CURRENT_DATE - INTERVAL '6 days' + TIME '08:00:00', 'superviseur_abj', 6, 1),
    (7, CURRENT_DATE - INTERVAL '5 days' + TIME '16:30:00', 'tech_koffi', 6, 6),
    (8, CURRENT_DATE - INTERVAL '8 days' + TIME '08:30:00', 'superviseur_int', 8, 1),
    (9, CURRENT_DATE - INTERVAL '7 days' + TIME '11:15:00', 'tech_ouattara', 8, 6);

-- =========================================================================
-- 9. DÉPÔTS, PIÈCES ET STOCKS
-- =========================================================================
INSERT INTO depot (id, nom) VALUES
    (1, 'Dépôt Central Abidjan'),
    (2, 'Dépôt Régional Bouaké'),
    (3, 'Dépôt Sud San-Pédro');

INSERT INTO piece (id, nom, reference, description) VALUES
    (1, 'Câble Fibre Optique Monomode G657A2', 'REF-FIB-001', 'Bobine 500m extérieure'),
    (2, 'Boîtier PTO Optique 1 port', 'REF-PTO-010', 'Prise terminale optique abonné'),
    (3, 'Disjoncteur Différentiel 30mA 16A', 'REF-DIS-016', 'Protection personnes et circuits'),
    (4, 'Switch Réseau Manageable 24 Ports PoE+', 'REF-SWI-024', 'Cisco Catalyst Gigabit'),
    (5, 'Compresseur Rotatif R410A', 'REF-COM-410', 'Unité de climatisation inverter');

INSERT INTO stock (id, quantite, depot_id, piece_id) VALUES
    (1, 150, 1, 1),
    (2, 300, 1, 2),
    (3, 85, 1, 3),
    (4, 25, 1, 4),
    (5, 12, 1, 5),
    (6, 60, 2, 1),
    (7, 40, 2, 3),
    (8, 10, 3, 5);

INSERT INTO intervention_piece (id, quantite_utilisee, type_intervention_id, piece_id) VALUES
    (1, 50, 1, 1), -- Raccordement fibre : 50m fibre
    (2, 1, 1, 2),  -- Raccordement fibre : 1 PTO
    (3, 2, 2, 3),  -- Maintenance électrique : 2 disjoncteurs
    (4, 1, 3, 4);  -- Installation baie : 1 switch manageable

-- =========================================================================
-- 10. ALERTES COMPÉTENCES PROCHES EXPIRATION (F16)
-- =========================================================================
-- Une alerte déjà traitée pour vérifier la règle de non-doublon F16
INSERT INTO alerte_competence (id, date_signalement, est_traitee, technicien_competence_id) VALUES
    (1, CURRENT_DATE - INTERVAL '2 days', true, 4);

-- =========================================================================
-- 11. SYNCHRONISATION DES SÉQUENCES IDENTITY POSTGRESQL
-- =========================================================================
SELECT setval('region_id_seq', COALESCE((SELECT MAX(id) FROM region), 1), true);
SELECT setval('departement_id_seq', COALESCE((SELECT MAX(id) FROM departement), 1), true);
SELECT setval('commune_id_seq', COALESCE((SELECT MAX(id) FROM commune), 1), true);
SELECT setval('agence_id_seq', COALESCE((SELECT MAX(id) FROM agence), 1), true);
SELECT setval('equipe_id_seq', COALESCE((SELECT MAX(id) FROM equipe), 1), true);
SELECT setval('technicien_id_seq', COALESCE((SELECT MAX(id) FROM technicien), 1), true);
SELECT setval('competence_id_seq', COALESCE((SELECT MAX(id) FROM competence), 1), true);
SELECT setval('technicien_competence_id_seq', COALESCE((SELECT MAX(id) FROM technicien_competence), 1), true);
SELECT setval('types_intervention_id_seq', COALESCE((SELECT MAX(id) FROM types_intervention), 1), true);
SELECT setval('creneau_id_seq', COALESCE((SELECT MAX(id) FROM creneau), 1), true);
SELECT setval('client_id_seq', COALESCE((SELECT MAX(id) FROM client), 1), true);
SELECT setval('statut_id_seq', COALESCE((SELECT MAX(id) FROM statut), 1), true);
SELECT setval('intervention_id_seq', COALESCE((SELECT MAX(id) FROM intervention), 1), true);
SELECT setval('intervention_creneau_id_seq', COALESCE((SELECT MAX(id) FROM intervention_creneau), 1), true);
SELECT setval('intervention_technicien_id_seq', COALESCE((SELECT MAX(id) FROM intervention_technicien), 1), true);
SELECT setval('depot_id_seq', COALESCE((SELECT MAX(id) FROM depot), 1), true);
SELECT setval('piece_id_seq', COALESCE((SELECT MAX(id) FROM piece), 1), true);
SELECT setval('stock_id_seq', COALESCE((SELECT MAX(id) FROM stock), 1), true);
SELECT setval('intervention_piece_id_seq', COALESCE((SELECT MAX(id) FROM intervention_piece), 1), true);
SELECT setval('historique_statut_id_seq', COALESCE((SELECT MAX(id) FROM historique_statut), 1), true);
SELECT setval('alerte_competence_id_seq', COALESCE((SELECT MAX(id) FROM alerte_competence), 1), true);
