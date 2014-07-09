SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';


-- -----------------------------------------------------
-- Table `metabolites`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `metabolites` ;

CREATE  TABLE IF NOT EXISTS `metabolites` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(150) NOT NULL ,
  `kegg_miriam` VARCHAR(100) NULL ,
  `kegg_name` VARCHAR(200) NULL ,
  `chebi_miriam` VARCHAR(100) NULL ,
  `chebi_name` VARCHAR(200) NULL ,
  `datatype` VARCHAR(45) NOT NULL ,
  `kegg_formula` VARCHAR(45) NULL ,
  `chebi_formula` VARCHAR(45) NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `taxonomy_data`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `taxonomy_data` ;

CREATE  TABLE IF NOT EXISTS `taxonomy_data` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `organism` TEXT NULL ,
  `taxonomy` TEXT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `general_equation`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `general_equation` ;

CREATE  TABLE IF NOT EXISTS `general_equation` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `equation` VARCHAR(450) NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `tc_numbers`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `tc_numbers` ;

CREATE  TABLE IF NOT EXISTS `tc_numbers` (
  `tc_number` VARCHAR(45) NOT NULL ,
  `tc_version` INT NOT NULL ,
  `taxonomy_data_id` INT NOT NULL ,
  `tc_family` VARCHAR(45) NOT NULL ,
  `tc_location` VARCHAR(45) NULL ,
  `affinity` VARCHAR(45) NULL ,
  `general_equation_id` INT NOT NULL ,
  INDEX `fk_tcnumber_taxonomy_data1_idx` (`taxonomy_data_id` ASC) ,
  INDEX `fk_tcnumber_general_equation1_idx` (`general_equation_id` ASC) ,
  PRIMARY KEY (`tc_number`, `tc_version`) ,
  CONSTRAINT `fk_tcnumber_taxonomy_data1`
    FOREIGN KEY (`taxonomy_data_id` )
    REFERENCES `taxonomy_data` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_tcnumber_general_equation1`
    FOREIGN KEY (`general_equation_id` )
    REFERENCES `general_equation` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `transport_types`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `transport_types` ;

CREATE  TABLE IF NOT EXISTS `transport_types` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(45) NULL ,
  `directions` VARCHAR(45) NULL ,
  `reversible` TINYINT(1) NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `transport_systems`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `transport_systems` ;

CREATE  TABLE IF NOT EXISTS `transport_systems` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `transport_type_id` INT NOT NULL ,
  `reversible` TINYINT(1) NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_transport_system_transport_type1_idx` (`transport_type_id` ASC) ,
  CONSTRAINT `fk_transport_system_transport_type1`
    FOREIGN KEY (`transport_type_id` )
    REFERENCES `transport_types` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `directions`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `directions` ;

CREATE  TABLE IF NOT EXISTS `directions` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `direction` VARCHAR(45) NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `transported_metabolites_directions`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `transported_metabolites_directions` ;

CREATE  TABLE IF NOT EXISTS `transported_metabolites_directions` (
  `metabolite_id` INT NOT NULL ,
  `direction_id` INT NOT NULL ,
  `transport_system_id` INT NOT NULL ,
  `stoichiometry` INT NOT NULL ,
  PRIMARY KEY (`metabolite_id`, `direction_id`, `transport_system_id`) ,
  INDEX `fk_metabolite_entries_has_metabolites_metabolites1_idx` (`metabolite_id` ASC) ,
  INDEX `fk_metabolite_entries_has_metabolites_direction1_idx` (`direction_id` ASC) ,
  INDEX `fk_transported_metabolites_direction_transportSystem1_idx` (`transport_system_id` ASC) ,
  CONSTRAINT `fk_metabolite_entries_has_metabolites_metabolites1`
    FOREIGN KEY (`metabolite_id` )
    REFERENCES `metabolites` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_metabolite_entries_has_metabolites_direction1`
    FOREIGN KEY (`direction_id` )
    REFERENCES `directions` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_transported_metabolites_direction_transportSystem1`
    FOREIGN KEY (`transport_system_id` )
    REFERENCES `transport_systems` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `synonyms`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synonyms` ;

CREATE  TABLE IF NOT EXISTS `synonyms` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `metabolite_id` INT NOT NULL ,
  `name` VARCHAR(100) NOT NULL ,
  `datatype` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_synonyms_metabolites1_idx` (`metabolite_id` ASC) ,
  CONSTRAINT `fk_synonyms_metabolites1`
    FOREIGN KEY (`metabolite_id` )
    REFERENCES `metabolites` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `projects`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `projects` ;

CREATE  TABLE IF NOT EXISTS `projects` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `organism_id` INT NOT NULL ,
  `latest_version` TINYINT(1) NULL ,
  `date` TIMESTAMP NULL ,
  `version` INT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `genes`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `genes` ;

CREATE  TABLE IF NOT EXISTS `genes` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `project_id` INT NOT NULL ,
  `locus_tag` VARCHAR(45) NOT NULL ,
  `status` ENUM('PROCESSED','PROCESSING') NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_genes_projects1_idx` (`project_id` ASC) ,
  CONSTRAINT `fk_genes_projects1`
    FOREIGN KEY (`project_id` )
    REFERENCES `projects` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `genes_has_metabolites`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `genes_has_metabolites` ;

CREATE  TABLE IF NOT EXISTS `genes_has_metabolites` (
  `gene_id` INT NOT NULL ,
  `metabolite_id` INT NOT NULL ,
  `similarity_score_sum` FLOAT NOT NULL ,
  `taxonomy_score_sum` FLOAT NOT NULL ,
  `frequency` INT NOT NULL ,
  PRIMARY KEY (`gene_id`, `metabolite_id`) ,
  INDEX `fk_genes_has_metabolites_metabolites1_idx` (`metabolite_id` ASC) ,
  INDEX `fk_genes_has_metabolites_genes1_idx` (`gene_id` ASC) ,
  CONSTRAINT `fk_genes_has_metabolites_genes1`
    FOREIGN KEY (`gene_id` )
    REFERENCES `genes` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_genes_has_metabolites_metabolites1`
    FOREIGN KEY (`metabolite_id` )
    REFERENCES `metabolites` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `tcdb_registries`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `tcdb_registries` ;

CREATE  TABLE IF NOT EXISTS `tcdb_registries` (
  `uniprot_id` VARCHAR(45) NOT NULL ,
  `version` INT NOT NULL ,
  `tc_number` VARCHAR(45) NOT NULL ,
  `tc_version` INT NOT NULL ,
  `loaded_at` TIMESTAMP NOT NULL ,
  `latest_version` TINYINT(1) NOT NULL ,
  `status` ENUM('PROCESSING','PROCESSED') NOT NULL ,
  PRIMARY KEY (`uniprot_id`, `version`) ,
  INDEX `fk_tcdb_registries_tc_numbers1_idx` (`tc_number` ASC, `tc_version` ASC) ,
  CONSTRAINT `fk_tcdb_registries_tc_numbers1`
    FOREIGN KEY (`tc_number` , `tc_version` )
    REFERENCES `tc_numbers` (`tc_number` , `tc_version` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `genes_has_tcdb_registries`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `genes_has_tcdb_registries` ;

CREATE  TABLE IF NOT EXISTS `genes_has_tcdb_registries` (
  `gene_id` INT NOT NULL ,
  `similarity` FLOAT NOT NULL ,
  `uniprot_id` VARCHAR(45) NOT NULL ,
  `version` INT NOT NULL ,
  PRIMARY KEY (`gene_id`, `uniprot_id`, `version`) ,
  INDEX `fk_genes_has_tcnumber_genes1_idx` (`gene_id` ASC) ,
  INDEX `fk_genes_has_tcnumber_tcdb_registries1_idx` (`uniprot_id` ASC, `version` ASC) ,
  CONSTRAINT `fk_genes_has_tcnumber_genes1`
    FOREIGN KEY (`gene_id` )
    REFERENCES `genes` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_genes_has_tcnumber_tcdb_registries1`
    FOREIGN KEY (`uniprot_id` , `version` )
    REFERENCES `tcdb_registries` (`uniprot_id` , `version` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `genes_has_metabolites_has_type`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `genes_has_metabolites_has_type` ;

CREATE  TABLE IF NOT EXISTS `genes_has_metabolites_has_type` (
  `gene_id` INT NOT NULL ,
  `metabolite_id` INT NOT NULL ,
  `transport_type_id` INT NOT NULL ,
  `transport_type_score_sum` FLOAT NOT NULL ,
  `taxonomy_score_sum` FLOAT NOT NULL ,
  `frequency` INT NOT NULL ,
  PRIMARY KEY (`gene_id`, `transport_type_id`, `metabolite_id`) ,
  INDEX `fk_genes_has_metabolites_has_type_genes_has_metabolites1_idx` (`gene_id` ASC, `metabolite_id` ASC) ,
  INDEX `fk_genes_has_metabolites_has_type_transport_type1_idx` (`transport_type_id` ASC) ,
  CONSTRAINT `fk_genes_has_metabolites_has_type_genes_has_metabolites1`
    FOREIGN KEY (`gene_id` , `metabolite_id` )
    REFERENCES `genes_has_metabolites` (`gene_id` , `metabolite_id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_genes_has_metabolites_has_type_transport_type1`
    FOREIGN KEY (`transport_type_id` )
    REFERENCES `transport_types` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `tc_numbers_has_transport_systems`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `tc_numbers_has_transport_systems` ;

CREATE  TABLE IF NOT EXISTS `tc_numbers_has_transport_systems` (
  `transport_system_id` INT NOT NULL ,
  `tc_number` VARCHAR(45) NOT NULL ,
  `tc_version` INT NOT NULL ,
  PRIMARY KEY (`transport_system_id`, `tc_number`, `tc_version`) ,
  INDEX `fk_tcnumber_has_transport_system_transport_system1_idx` (`transport_system_id` ASC) ,
  INDEX `fk_tcnumbers_has_transport_system_tc_numbers1_idx` (`tc_number` ASC, `tc_version` ASC) ,
  CONSTRAINT `fk_tcnumber_has_transport_system_transport_system1`
    FOREIGN KEY (`transport_system_id` )
    REFERENCES `transport_systems` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_tcnumbers_has_transport_system_tc_numbers1`
    FOREIGN KEY (`tc_number` , `tc_version` )
    REFERENCES `tc_numbers` (`tc_number` , `tc_version` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `metabolites_ontology`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `metabolites_ontology` ;

CREATE  TABLE IF NOT EXISTS `metabolites_ontology` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `metabolite_id` INT NOT NULL ,
  `child_id` INT NOT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Placeholder table for view `gene_to_metabolite_direction`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `gene_to_metabolite_direction` (`gene_id` INT, `locus_tag` INT, `tc_family` INT, `transport_type` INT, `transport_reaction_id` INT, `metabolite_id` INT, `metabolite_name` INT, `metabolite_kegg_name` INT, `stoichiometry` INT, `kegg_miriam` INT, `direction` INT, `reversible` INT, `chebi_miriam` INT, `tc_number` INT, `uniprot_id` INT, `similarity` INT, `taxonomy_data_id` INT, `project_id` INT);

-- -----------------------------------------------------
-- procedure getMetaboliteTaxonomyScores
-- -----------------------------------------------------
DROP procedure IF EXISTS `getMetaboliteTaxonomyScores`;

DELIMITER $$
CREATE PROCEDURE getMetaboliteTaxonomyScores (IN originTaxonomy BIGINT UNSIGNED, IN minimal_hits BIGINT UNSIGNED, IN alpha FLOAT UNSIGNED, IN beta_penalty FLOAT UNSIGNED, IN idproject BIGINT UNSIGNED)
BEGIN
   SELECT metabolite_id, gene_id, similarity_score_sum/(
       SELECT SUM(similarity)
       FROM genes_has_tcdb_registries
       INNER JOIN genes ON genes.id = genes_has_tcdb_registries.gene_id
       INNER JOIN tcdb_registries ON (genes_has_tcdb_registries.uniprot_id=tcdb_registries.uniprot_id AND genes_has_tcdb_registries.version=tcdb_registries.version)
       WHERE project_id = idproject AND latest_version AND genes_has_tcdb_registries.gene_id = genes_has_metabolites.gene_id)
   *alpha+(1-alpha)*
   (taxonomy_score_sum*(1-(minimal_hits-getFrequency(frequency,minimal_hits))*beta_penalty)/(originTaxonomy*frequency)) as final_score
   FROM genes_has_metabolites;
  /*WHERE genes_id = geneid;*/
END 
$$

-- -----------------------------------------------------
-- procedure getTransportTypeTaxonomyScore
-- -----------------------------------------------------
DROP procedure IF EXISTS `getTransportTypeTaxonomyScore`;

DELIMITER $$


CREATE PROCEDURE getTransportTypeTaxonomyScore (IN originTaxonomy BIGINT UNSIGNED, IN minimal_hits BIGINT UNSIGNED, IN alpha FLOAT UNSIGNED, IN beta_penalty FLOAT UNSIGNED, IN idproject BIGINT UNSIGNED)
BEGIN
   SELECT transport_type_id, metabolite_id, gene_id, transport_type_score_sum/(
       SELECT SUM(similarity)
       FROM genes_has_tcdb_registries
       INNER JOIN genes_has_metabolites ON genes_has_tcdb_registries.gene_id=genes_has_metabolites.gene_id
	   INNER JOIN genes ON genes.id = genes_has_tcdb_registries.gene_id
       INNER JOIN tcdb_registries ON (genes_has_tcdb_registries.uniprot_id=tcdb_registries.uniprot_id AND genes_has_tcdb_registries.version=tcdb_registries.version)
       WHERE project_id = idproject AND latest_version AND genes_has_tcdb_registries.gene_id = genes_has_metabolites_has_type.gene_id
       AND genes_has_metabolites.metabolite_id=genes_has_metabolites_has_type.metabolite_id)
	   *alpha+(1-alpha)*
	  (genes_has_metabolites_has_type.taxonomy_score_sum*(1-(minimal_hits-getFrequency(genes_has_metabolites_has_type.frequency,minimal_hits))*beta_penalty)/(originTaxonomy*genes_has_metabolites_has_type.frequency)) as final_score
   FROM genes_has_metabolites_has_type
   ORDER BY gene_id , metabolite_id , transport_type_id;
/*   WHERE metabolites_id=metabolitesid;*/
  END 
$$

-- -----------------------------------------------------
-- function getFrequency
-- -----------------------------------------------------
DROP function IF EXISTS `getFrequency`;

DELIMITER $$


CREATE FUNCTION getFrequency(frequency INT, minimal_hits INT)
  RETURNS INT
  BEGIN
    DECLARE result INT(11);

    IF frequency > minimal_hits THEN SET result = minimal_hits;
    ELSE SET result = frequency;
    END IF;

    RETURN result;
  END 
$$

-- -----------------------------------------------------
-- View `gene_to_metabolite_direction`
-- -----------------------------------------------------
DROP VIEW IF EXISTS `gene_to_metabolite_direction` ;
DROP TABLE IF EXISTS `gene_to_metabolite_direction`;
CREATE  OR REPLACE VIEW gene_to_metabolite_direction AS 
SELECT g.id as gene_id,
g.locus_tag,
tc.tc_family as tc_family,
tt.name as transport_type,
ts.id as transport_reaction_id,
m.id as metabolite_id,
m.name as metabolite_name,
m.kegg_name  as metabolite_kegg_name,
tmd.stoichiometry as stoichiometry,
m.kegg_miriam,
d.direction,
ts.reversible,
m.chebi_miriam AS chebi_miriam,
tc.tc_number as tc_number,
tr.uniprot_id as uniprot_id,
ghtr.similarity as similarity,
taxonomy_data_id,
project_id
FROM genes_has_tcdb_registries as ghtr
INNER JOIN genes as g on g.id = ghtr.gene_id
INNER JOIN tcdb_registries as tr on ghtr.uniprot_id=tr.uniprot_id AND ghtr.version=tr.version
INNER JOIN tc_numbers as tc on tr.tc_number=tc.tc_number AND tc.tc_version=tr.tc_version
INNER JOIN tc_numbers_has_transport_systems as tc_ts on  tc.tc_number = tc_ts.tc_number
INNER JOIN transport_systems as ts on  tc_ts.transport_system_id= ts.id
INNER JOIN transport_types as tt on ts.transport_type_id = tt.id
INNER JOIN transported_metabolites_directions as tmd on ts.id = tmd.transport_system_id
INNER JOIN metabolites as m on m.id=tmd.metabolite_id
INNER JOIN directions as d on d.id=tmd.direction_id;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
