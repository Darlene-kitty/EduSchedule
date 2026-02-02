-- ============================================
-- School Service - Tables Creation and Updates
-- ============================================

-- Update schools table to match entity
ALTER TABLE schools 
ADD COLUMN IF NOT EXISTS address VARCHAR(255),
ADD COLUMN IF NOT EXISTS phone VARCHAR(20),
ADD COLUMN IF NOT EXISTS email VARCHAR(100),
ADD COLUMN IF NOT EXISTS description TEXT,
ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Create niveaux table
CREATE TABLE IF NOT EXISTS niveaux (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20),
    ordre INT,
    filiere_id BIGINT NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (filiere_id) REFERENCES filieres(id) ON DELETE CASCADE,
    INDEX idx_niveaux_filiere (filiere_id)
);

-- Create affectations table
CREATE TABLE IF NOT EXISTS affectations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    etudiant_id BIGINT NOT NULL,
    groupe_id BIGINT NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE,
    active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (groupe_id) REFERENCES groupes(id) ON DELETE CASCADE,
    INDEX idx_affectations_etudiant (etudiant_id),
    INDEX idx_affectations_groupe (groupe_id)
);

-- Insert sample data for niveaux
INSERT INTO niveaux (name, code, ordre, filiere_id, active) VALUES 
('Licence 1', 'L1', 1, 1, TRUE),
('Licence 2', 'L2', 2, 1, TRUE),
('Licence 3', 'L3', 3, 1, TRUE),
('Master 1', 'M1', 4, 1, TRUE),
('Master 2', 'M2', 5, 1, TRUE)
ON DUPLICATE KEY UPDATE name=name;

-- Update existing schools data
UPDATE schools SET 
    active = TRUE,
    created_at = COALESCE(created_at, NOW()),
    updated_at = NOW()
WHERE active IS NULL;

SELECT 'School Service tables created/updated successfully!' as status;