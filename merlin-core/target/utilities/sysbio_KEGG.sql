SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';


-- -----------------------------------------------------
-- Table `ri_function`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ri_function` ;

CREATE  TABLE IF NOT EXISTS `ri_function` (
  `idri_function` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `symbol` CHAR(2) NULL ,
  `ri_function` CHAR(20) NULL ,
  PRIMARY KEY (`idri_function`) )
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `chromosome`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `chromosome` ;

CREATE  TABLE IF NOT EXISTS `chromosome` (
  `idchromosome` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(15) NOT NULL ,
  PRIMARY KEY (`idchromosome`) )
ENGINE = InnoDB
PACK_KEYS = DEFAULT
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `gene`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `gene` ;

CREATE  TABLE IF NOT EXISTS `gene` (
  `idgene` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `chromosome_idchromosome` INT UNSIGNED NOT NULL ,
  `name` VARCHAR(120) NULL ,
  `locusTag` VARCHAR(45) NULL ,
  `transcription_direction` CHAR(3) NULL ,
  `left_end_position` VARCHAR(45) NULL ,
  `right_end_position` VARCHAR(100) NULL ,
  `boolean_rule` VARCHAR(200) NULL ,
  `origin` ENUM('HOMOLOGY','MANUAL','KEGG','TRANSPORTERS','KO') NOT NULL ,
  PRIMARY KEY (`idgene`) ,
  INDEX `gene_name` (`name` ASC) ,
  CONSTRAINT `fk_{7B538B83-2018-4A9C-A278-D9A3CDBA0062}`
    FOREIGN KEY (`chromosome_idchromosome` )
    REFERENCES `chromosome` (`idchromosome` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `sequence`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sequence` ;

CREATE  TABLE IF NOT EXISTS `sequence` (
  `idsequence` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `gene_idgene` INT UNSIGNED NOT NULL ,
  `sequence_type` VARCHAR(10) NULL ,
  `sequence` TEXT NULL ,
  `sequence_length` INT UNSIGNED NULL ,
  PRIMARY KEY (`idsequence`) ,
  CONSTRAINT `fk_{D8A67108-DC04-4A90-89A3-AE29AF45AF51}`
    FOREIGN KEY (`gene_idgene` )
    REFERENCES `gene` (`idgene` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `pathway`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pathway` ;

CREATE  TABLE IF NOT EXISTS `pathway` (
  `idpathway` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `code` VARCHAR(5) NOT NULL ,
  `name` VARCHAR(120) NOT NULL ,
  `path_sbml_file` VARCHAR(200) NULL ,
  `image` BLOB NULL ,
  PRIMARY KEY (`idpathway`) ,
  INDEX `name` (`name` ASC) )
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `compound`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `compound` ;

CREATE  TABLE IF NOT EXISTS `compound` (
  `idcompound` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(255) NULL ,
  `inchi` VARCHAR(1500) NULL ,
  `kegg_id` VARCHAR(15) NULL ,
  `entry_type` VARCHAR(9) NULL ,
  `formula` TEXT NULL ,
  `molecular_weight` VARCHAR(100) NULL ,
  `neutral_formula` VARCHAR(120) NULL ,
  `charge` SMALLINT UNSIGNED NULL ,
  `smiles` VARCHAR(1200) NULL ,
  `hasBiologicalRoles` TINYINT(1) NULL ,
  PRIMARY KEY (`idcompound`) )
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `compartment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `compartment` ;

CREATE  TABLE IF NOT EXISTS `compartment` (
  `idcompartment` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(25) NULL ,
  `abbreviation` VARCHAR(10) NULL ,
  PRIMARY KEY (`idcompartment`) )
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `reaction`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `reaction` ;

CREATE  TABLE IF NOT EXISTS `reaction` (
  `idreaction` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(200) NULL ,
  `equation` VARCHAR(500) NULL ,
  `reversible` TINYINT(1) NULL ,
  `boolean_rule` VARCHAR(400) NULL ,
  `inModel` TINYINT(1) NULL ,
  `isGeneric` TINYINT(1) NULL ,
  `isSpontaneous` TINYINT(1) NULL ,
  `isNonEnzymatic` TINYINT(1) NULL ,
  `source` VARCHAR(45) NOT NULL ,
  `originalReaction` TINYINT(1) NOT NULL ,
  `compartment_idcompartment` INT UNSIGNED NULL ,
  `notes` TEXT NULL ,
  `lowerBound` BIGINT NULL,
  `upperBound` BIGINT NULL,
  PRIMARY KEY (`idreaction`, `compartment_idcompartment`) ,
  INDEX `name` (`name` ASC) ,
  INDEX `equation` (`equation` ASC) ,
  INDEX `reversible` (`reversible` ASC) ,
  CONSTRAINT `fk_reaction_compartment1`
    FOREIGN KEY (`compartment_idcompartment` )
    REFERENCES `compartment` (`idcompartment` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `stoichiometry`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `stoichiometry` ;

CREATE  TABLE IF NOT EXISTS `stoichiometry` (
  `idstoichiometry` INT NOT NULL AUTO_INCREMENT ,
  `reaction_idreaction` INT UNSIGNED NOT NULL ,
  `compound_idcompound` INT UNSIGNED NOT NULL ,
  `compartment_idcompartment` INT UNSIGNED NOT NULL ,
  `stoichiometric_coefficient` VARCHAR(10) NOT NULL ,
  `numberofchains` VARCHAR(10) NULL ,
  PRIMARY KEY (`idstoichiometry`, `reaction_idreaction`, `compound_idcompound`, `compartment_idcompartment`) ,
  CONSTRAINT `fk_{D30155E9-D232-4AAB-A7A1-21CEA3DCA687}`
    FOREIGN KEY (`compound_idcompound` )
    REFERENCES `compound` (`idcompound` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{6C94826D-DB02-419D-8759-E2768D2FC57A}`
    FOREIGN KEY (`reaction_idreaction` )
    REFERENCES `reaction` (`idreaction` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{0EC05BD7-7F90-4231-B693-23833490F81C}`
    FOREIGN KEY (`compartment_idcompartment` )
    REFERENCES `compartment` (`idcompartment` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `feature`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `feature` ;

CREATE  TABLE IF NOT EXISTS `feature` (
  `idfeature` INT UNSIGNED NOT NULL ,
  `class` VARCHAR(50) NULL ,
  `description` VARCHAR(1300) NULL ,
  `start_position` INT UNSIGNED NULL ,
  `end_position` INT UNSIGNED NULL ,
  PRIMARY KEY (`idfeature`) )
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `sequence_feature`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sequence_feature` ;

CREATE  TABLE IF NOT EXISTS `sequence_feature` (
  `feature_idfeature` INT UNSIGNED NOT NULL ,
  `sequence_idsequence` INT UNSIGNED NOT NULL ,
  `start_position_approximate` VARCHAR(10) NULL ,
  `end_position_approximate` VARCHAR(10) NULL ,
  PRIMARY KEY (`feature_idfeature`) ,
  INDEX `sequence_feature_FKIndex1` (`feature_idfeature` ASC) ,
  CONSTRAINT `fk_{86422969-ACAE-4B85-9A69-F57125E3EE29}`
    FOREIGN KEY (`feature_idfeature` )
    REFERENCES `feature` (`idfeature` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{DAF2C5B0-CE7A-4CB7-8F82-DB1FF2466628}`
    FOREIGN KEY (`sequence_idsequence` )
    REFERENCES `sequence` (`idsequence` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `transcription_unit`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `transcription_unit` ;

CREATE  TABLE IF NOT EXISTS `transcription_unit` (
  `idtranscription_unit` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(150) NULL ,
  PRIMARY KEY (`idtranscription_unit`) )
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `transcription_unit_gene`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `transcription_unit_gene` ;

CREATE  TABLE IF NOT EXISTS `transcription_unit_gene` (
  `transcription_unit_idtranscription_unit` INT UNSIGNED NOT NULL ,
  `gene_idgene` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`transcription_unit_idtranscription_unit`, `gene_idgene`) ,
  CONSTRAINT `fk_{9BD537C5-CDA3-45A3-AD95-6C5D56D655DC}`
    FOREIGN KEY (`transcription_unit_idtranscription_unit` )
    REFERENCES `transcription_unit` (`idtranscription_unit` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{71365A90-D93F-44DB-87B9-041AC8D718F6}`
    FOREIGN KEY (`gene_idgene` )
    REFERENCES `gene` (`idgene` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `protein`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `protein` ;

CREATE  TABLE IF NOT EXISTS `protein` (
  `idprotein` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(255) NOT NULL ,
  `class` VARCHAR(120) NULL ,
  `inchi` VARCHAR(255) NULL ,
  `molecular_weight` FLOAT NULL ,
  `molecular_weight_exp` FLOAT NULL ,
  `molecular_weight_kd` FLOAT NULL ,
  `molecular_weight_seq` FLOAT NULL ,
  `pi` FLOAT NULL ,
  PRIMARY KEY (`idprotein`) ,
  INDEX `idprotein` (`idprotein` ASC) )
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `enzyme`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `enzyme` ;

CREATE  TABLE IF NOT EXISTS `enzyme` (
  `ecnumber` VARCHAR(15) NOT NULL ,
  `protein_idprotein` INT UNSIGNED NOT NULL ,
  `optimal_ph` FLOAT NULL ,
  `posttranslational_modification` VARCHAR(100) NULL ,
  `inModel` TINYINT(1) NULL ,
  `source` VARCHAR(45) NULL ,
  PRIMARY KEY (`ecnumber`, `protein_idprotein`) ,
  CONSTRAINT `fk_enzyme_protein1`
    FOREIGN KEY (`protein_idprotein` )
    REFERENCES `protein` (`idprotein` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `reaction_has_enzyme`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `reaction_has_enzyme` ;

CREATE  TABLE IF NOT EXISTS `reaction_has_enzyme` (
  `reaction_idreaction` INT UNSIGNED NOT NULL ,
  `enzyme_ecnumber` VARCHAR(15) NOT NULL ,
  `enzyme_protein_idprotein` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`reaction_idreaction`, `enzyme_ecnumber`, `enzyme_protein_idprotein`) ,
  CONSTRAINT `fk_{1ED09979-1E8D-46E1-B0B1-FBF8F1F9E41C}`
    FOREIGN KEY (`reaction_idreaction` )
    REFERENCES `reaction` (`idreaction` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_reaction_has_enzyme_enzyme1`
    FOREIGN KEY (`enzyme_ecnumber` , `enzyme_protein_idprotein` )
    REFERENCES `enzyme` (`ecnumber` , `protein_idprotein` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `strain`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `strain` ;

CREATE  TABLE IF NOT EXISTS `strain` (
  `idstrain` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(60) NULL ,
  PRIMARY KEY (`idstrain`) )
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `entityisfrom`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `entityisfrom` ;

CREATE  TABLE IF NOT EXISTS `entityisfrom` (
  `strain_idstrain` INT UNSIGNED NOT NULL ,
  `wid` INT UNSIGNED NULL ,
  CONSTRAINT `fk_{E0FC3994-9B19-4890-9B38-6C5A303836AE}`
    FOREIGN KEY (`strain_idstrain` )
    REFERENCES `strain` (`idstrain` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `enzymatic_cofactor`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `enzymatic_cofactor` ;

CREATE  TABLE IF NOT EXISTS `enzymatic_cofactor` (
  `compound_idcompound` INT UNSIGNED NOT NULL ,
  `protein_idprotein` INT UNSIGNED NOT NULL ,
  `prosthetic` TINYINT(1) NULL ,
  CONSTRAINT `fk_{C334ABF4-A44F-4E5C-8A65-E84AF763B37B}`
    FOREIGN KEY (`compound_idcompound` )
    REFERENCES `compound` (`idcompound` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{F55662E4-4022-4A5E-AE2A-CDDFD370896C}`
    FOREIGN KEY (`protein_idprotein` )
    REFERENCES `protein` (`idprotein` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `promoter`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `promoter` ;

CREATE  TABLE IF NOT EXISTS `promoter` (
  `idpromoter` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(50) NULL ,
  `absolute_position` DOUBLE NULL ,
  PRIMARY KEY (`idpromoter`) )
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `regulatory_event`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `regulatory_event` ;

CREATE  TABLE IF NOT EXISTS `regulatory_event` (
  `protein_idprotein` INT UNSIGNED NOT NULL ,
  `promoter_idpromoter` INT UNSIGNED NOT NULL ,
  `ri_function_idri_function` INT UNSIGNED NOT NULL ,
  `binding_site_position` DECIMAL(15,6) NULL ,
  PRIMARY KEY (`protein_idprotein`, `promoter_idpromoter`, `ri_function_idri_function`) ,
  CONSTRAINT `fk_{A7DB4601-9694-4D89-8359-3D68915FDA8E}`
    FOREIGN KEY (`promoter_idpromoter` )
    REFERENCES `promoter` (`idpromoter` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{7A0F1F10-3B2B-483B-B11D-2D25DF875A45}`
    FOREIGN KEY (`protein_idprotein` )
    REFERENCES `protein` (`idprotein` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{62261738-C32F-40F5-A799-C42AF0257507}`
    FOREIGN KEY (`ri_function_idri_function` )
    REFERENCES `ri_function` (`idri_function` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `enzymatic_alternative_cofactor`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `enzymatic_alternative_cofactor` ;

CREATE  TABLE IF NOT EXISTS `enzymatic_alternative_cofactor` (
  `compound_idcompound` INT UNSIGNED NOT NULL ,
  `original_cofactor` INT UNSIGNED NOT NULL ,
  `protein_idprotein` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`compound_idcompound`, `original_cofactor`, `protein_idprotein`) ,
  CONSTRAINT `fk_{42AD4B34-5EC4-4F0A-A86E-52A567E51F1E}`
    FOREIGN KEY (`compound_idcompound` )
    REFERENCES `compound` (`idcompound` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{E728E8CC-B227-4951-832B-F01441866D86}`
    FOREIGN KEY (`protein_idprotein` )
    REFERENCES `protein` (`idprotein` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `module`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `module` ;

CREATE  TABLE IF NOT EXISTS `module` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `reaction` TEXT NOT NULL ,
  `entry_id` VARCHAR(6) NOT NULL ,
  `stoichiometry` VARCHAR(45) NULL ,
  `name` VARCHAR(200) NULL ,
  `definition` TEXT NOT NULL ,
  `hieralchical_class` TEXT NULL ,
  `type` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `protein_complex`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `protein_complex` ;

CREATE  TABLE IF NOT EXISTS `protein_complex` (
  `idprotein_complex` INT NOT NULL ,
  `name` TEXT NULL ,
  PRIMARY KEY (`idprotein_complex`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `subunit`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `subunit` ;

CREATE  TABLE IF NOT EXISTS `subunit` (
  `gene_idgene` INT UNSIGNED NOT NULL ,
  `enzyme_protein_idprotein` INT UNSIGNED NOT NULL ,
  `enzyme_ecnumber` VARCHAR(15) NOT NULL ,
  `module_id` INT NULL ,
  `protein_complex_idprotein_complex` INT NULL ,
  `note` VARCHAR(45) NULL ,
  `gpr_status` VARCHAR(45) NULL ,
  CONSTRAINT `fk_{2104F2C4-7C70-44F0-947A-2DE6A0D6E3F5}`
    FOREIGN KEY (`gene_idgene` )
    REFERENCES `gene` (`idgene` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_subunit_module1`
    FOREIGN KEY (`module_id` )
    REFERENCES `module` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_subunit_protein_complex1`
    FOREIGN KEY (`protein_complex_idprotein_complex` )
    REFERENCES `protein_complex` (`idprotein_complex` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_subunit_enzyme1`
    FOREIGN KEY (`enzyme_ecnumber` , `enzyme_protein_idprotein` )
    REFERENCES `enzyme` (`ecnumber` , `protein_idprotein` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `protein_composition`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `protein_composition` ;

CREATE  TABLE IF NOT EXISTS `protein_composition` (
  `protein_idprotein` INT UNSIGNED NOT NULL ,
  `subunit` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`protein_idprotein`, `subunit`) ,
  CONSTRAINT `fk_{53323DDA-C95E-490E-8BD4-A75D1D8C9811}`
    FOREIGN KEY (`protein_idprotein` )
    REFERENCES `protein` (`idprotein` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `functional_parameter`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `functional_parameter` ;

CREATE  TABLE IF NOT EXISTS `functional_parameter` (
  `compound_idcompound` INT UNSIGNED NOT NULL ,
  `protein_idprotein` INT UNSIGNED NOT NULL ,
  `parameter_type` VARCHAR(50) NOT NULL ,
  `parameter_value` FLOAT NULL ,
  PRIMARY KEY (`compound_idcompound`, `protein_idprotein`, `parameter_type`) ,
  CONSTRAINT `fk_{3CC44288-B2C2-4568-91EF-F5AC414B3BBF}`
    FOREIGN KEY (`compound_idcompound` )
    REFERENCES `compound` (`idcompound` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{D7B1CDD2-2312-45EC-BE9D-651B1D9B2767}`
    FOREIGN KEY (`protein_idprotein` )
    REFERENCES `protein` (`idprotein` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `substrate_affinity`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `substrate_affinity` ;

CREATE  TABLE IF NOT EXISTS `substrate_affinity` (
  `compound_idcompound` INT UNSIGNED NOT NULL ,
  `protein_idprotein` INT UNSIGNED NOT NULL ,
  `substrate_affinity` FLOAT NOT NULL ,
  PRIMARY KEY (`compound_idcompound`, `protein_idprotein`) ,
  CONSTRAINT `fk_{A4A5F57B-25AF-4290-89D3-F7BB6B29008F}`
    FOREIGN KEY (`compound_idcompound` )
    REFERENCES `compound` (`idcompound` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{AF3C5859-D2CC-495F-946F-2012EBBA060F}`
    FOREIGN KEY (`protein_idprotein` )
    REFERENCES `protein` (`idprotein` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `pathway_has_reaction`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pathway_has_reaction` ;

CREATE  TABLE IF NOT EXISTS `pathway_has_reaction` (
  `reaction_idreaction` INT UNSIGNED NOT NULL ,
  `pathway_idpathway` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`reaction_idreaction`, `pathway_idpathway`) ,
  CONSTRAINT `fk_{1737AEA1-4836-4E90-B0EB-F5FE39E2B64F}`
    FOREIGN KEY (`reaction_idreaction` )
    REFERENCES `reaction` (`idreaction` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{00E9536F-CFCB-4429-9E47-61F9E905994A}`
    FOREIGN KEY (`pathway_idpathway` )
    REFERENCES `pathway` (`idpathway` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `experimental_factor`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `experimental_factor` ;

CREATE  TABLE IF NOT EXISTS `experimental_factor` (
  `idexperimental_factor` INT UNSIGNED NOT NULL ,
  `factor` VARCHAR(255) NULL ,
  PRIMARY KEY (`idexperimental_factor`) )
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `experiment_description`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `experiment_description` ;

CREATE  TABLE IF NOT EXISTS `experiment_description` (
  `idexperiment` INT UNSIGNED NOT NULL ,
  `experiment_descriptional_factor_idexperimental_factor` INT UNSIGNED NOT NULL ,
  `value` VARCHAR(255) NOT NULL ,
  PRIMARY KEY (`idexperiment`, `experiment_descriptional_factor_idexperimental_factor`) ,
  CONSTRAINT `fk_{DE05BB66-C859-485C-A4F6-8607B45F571A}`
    FOREIGN KEY (`experiment_descriptional_factor_idexperimental_factor` )
    REFERENCES `experimental_factor` (`idexperimental_factor` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `experiment_substrate_affinity`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `experiment_substrate_affinity` ;

CREATE  TABLE IF NOT EXISTS `experiment_substrate_affinity` (
  `experiment_description` INT UNSIGNED NOT NULL ,
  `compound_idcompound` INT UNSIGNED NOT NULL ,
  `protein_idprotein` INT UNSIGNED NOT NULL ,
  `substrate_affinity` FLOAT NULL ,
  PRIMARY KEY (`experiment_description`, `compound_idcompound`, `protein_idprotein`) ,
  CONSTRAINT `fk_{D9D1EAE7-2FB6-424B-9A8A-1BFE733D13F0}`
    FOREIGN KEY (`compound_idcompound` )
    REFERENCES `compound` (`idcompound` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{F79D6F82-E185-4F20-A05E-F3B1F7B204B2}`
    FOREIGN KEY (`protein_idprotein` )
    REFERENCES `protein` (`idprotein` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `experiment_inhibitor`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `experiment_inhibitor` ;

CREATE  TABLE IF NOT EXISTS `experiment_inhibitor` (
  `compound_idcompound` INT UNSIGNED NOT NULL ,
  `experiment_description` INT UNSIGNED NOT NULL ,
  `protein_idprotein` INT UNSIGNED NOT NULL ,
  `ki` FLOAT NULL ,
  PRIMARY KEY (`compound_idcompound`, `experiment_description`, `protein_idprotein`) ,
  CONSTRAINT `fk_{B315DE73-0852-41A4-A611-493D45249B1D}`
    FOREIGN KEY (`compound_idcompound` )
    REFERENCES `compound` (`idcompound` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{92FD6A9A-16C1-419E-95F1-D6658D5C6A54}`
    FOREIGN KEY (`protein_idprotein` )
    REFERENCES `protein` (`idprotein` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `experiment_turnover_number`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `experiment_turnover_number` ;

CREATE  TABLE IF NOT EXISTS `experiment_turnover_number` (
  `experiment_description` INT UNSIGNED NOT NULL ,
  `compound_idcompound` INT UNSIGNED NOT NULL ,
  `protein_idprotein` INT UNSIGNED NOT NULL ,
  `turnover_number` FLOAT NULL ,
  PRIMARY KEY (`experiment_description`, `compound_idcompound`, `protein_idprotein`) ,
  CONSTRAINT `fk_{6CE40975-9221-4733-B659-0BF645E2FEF0}`
    FOREIGN KEY (`compound_idcompound` )
    REFERENCES `compound` (`idcompound` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{B2AC56B4-C8B2-407D-8EA7-0F4407E354EE}`
    FOREIGN KEY (`protein_idprotein` )
    REFERENCES `protein` (`idprotein` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `transcription_unit_promoter`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `transcription_unit_promoter` ;

CREATE  TABLE IF NOT EXISTS `transcription_unit_promoter` (
  `promoter_idpromoter` INT UNSIGNED NOT NULL ,
  `transcription_unit_idtranscription_unit` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`promoter_idpromoter`, `transcription_unit_idtranscription_unit`) ,
  CONSTRAINT `fk_{03CFFC6D-BE07-4F47-8F06-3EB4DC421485}`
    FOREIGN KEY (`promoter_idpromoter` )
    REFERENCES `promoter` (`idpromoter` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{387CE96A-A914-42D6-94A9-E2B58D5CF587}`
    FOREIGN KEY (`transcription_unit_idtranscription_unit` )
    REFERENCES `transcription_unit` (`idtranscription_unit` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `sigma_promoter`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sigma_promoter` ;

CREATE  TABLE IF NOT EXISTS `sigma_promoter` (
  `protein_idprotein` INT UNSIGNED NOT NULL ,
  `promoter_idpromoter` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`protein_idprotein`, `promoter_idpromoter`) ,
  CONSTRAINT `fk_{FC3BDF90-BF8B-4AD2-BDC8-FA922AE5958F}`
    FOREIGN KEY (`protein_idprotein` )
    REFERENCES `protein` (`idprotein` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{342E11D9-7A1D-446F-B40F-F09B6A854984}`
    FOREIGN KEY (`promoter_idpromoter` )
    REFERENCES `promoter` (`idpromoter` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `metabolic_regulation`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `metabolic_regulation` ;

CREATE  TABLE IF NOT EXISTS `metabolic_regulation` (
  `compound_idcompound` INT UNSIGNED NOT NULL ,
  `protein_idprotein` INT UNSIGNED NOT NULL ,
  `mode` CHAR(1) NULL ,
  `mechanism` VARCHAR(25) NULL ,
  PRIMARY KEY (`compound_idcompound`, `protein_idprotein`) ,
  CONSTRAINT `fk_{B3D1468D-19DF-46B6-AB62-AADFD00988C7}`
    FOREIGN KEY (`protein_idprotein` )
    REFERENCES `protein` (`idprotein` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{F6C47BF2-CBEE-4453-BF86-E8ABCF475CB1}`
    FOREIGN KEY (`compound_idcompound` )
    REFERENCES `compound` (`idcompound` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `activating_reaction`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `activating_reaction` ;

CREATE  TABLE IF NOT EXISTS `activating_reaction` (
  `reaction_idreaction` INT UNSIGNED NOT NULL ,
  `enzyme_ecnumber` VARCHAR(15) NOT NULL ,
  `enzyme_protein_idprotein` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`reaction_idreaction`, `enzyme_ecnumber`, `enzyme_protein_idprotein`) ,
  CONSTRAINT `fk_{F560DACF-3799-4E20-ACF9-8681138175A3}`
    FOREIGN KEY (`reaction_idreaction` )
    REFERENCES `reaction` (`idreaction` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_activating_reaction_enzyme1`
    FOREIGN KEY (`enzyme_ecnumber` , `enzyme_protein_idprotein` )
    REFERENCES `enzyme` (`ecnumber` , `protein_idprotein` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `dictionary`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `dictionary` ;

CREATE  TABLE IF NOT EXISTS `dictionary` (
  `class` VARCHAR(1) NOT NULL ,
  `aliases` VARCHAR(250) NOT NULL ,
  `common_name` VARCHAR(250) NULL ,
  PRIMARY KEY (`class`, `aliases`) )
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `aliases`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `aliases` ;

CREATE  TABLE IF NOT EXISTS `aliases` (
  `idalias` INT NOT NULL AUTO_INCREMENT ,
  `class` VARCHAR(2) NOT NULL ,
  `entity` INT UNSIGNED NOT NULL ,
  `alias` VARCHAR(1200) NOT NULL ,
  PRIMARY KEY (`idalias`, `class`, `entity`) )
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `dblinks`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `dblinks` ;

CREATE  TABLE IF NOT EXISTS `dblinks` (
  `class` VARCHAR(2) NOT NULL ,
  `internal_id` INT UNSIGNED NOT NULL ,
  `external_database` VARCHAR(150) NOT NULL ,
  `external_id` VARCHAR(150) NOT NULL ,
  PRIMARY KEY (`class`, `internal_id`, `external_database`, `external_id`) )
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `effector`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `effector` ;

CREATE  TABLE IF NOT EXISTS `effector` (
  `protein_idprotein` INT UNSIGNED NOT NULL ,
  `compound_idcompound` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`protein_idprotein`, `compound_idcompound`) ,
  CONSTRAINT `fk_{759262EC-9824-438A-967C-2CB349E6FB37}`
    FOREIGN KEY (`compound_idcompound` )
    REFERENCES `compound` (`idcompound` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_{627365FC-18A8-4F3C-A01F-1BCDCC6E5623}`
    FOREIGN KEY (`protein_idprotein` )
    REFERENCES `protein` (`idprotein` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `pathway_has_compound`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pathway_has_compound` ;

CREATE  TABLE IF NOT EXISTS `pathway_has_compound` (
  `pathway_idpathway` INT UNSIGNED NOT NULL ,
  `compound_idcompound` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`pathway_idpathway`, `compound_idcompound`) ,
  CONSTRAINT `fk_pathway_has_compound_pathway1`
    FOREIGN KEY (`pathway_idpathway` )
    REFERENCES `pathway` (`idpathway` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_pathway_has_compound_compound1`
    FOREIGN KEY (`compound_idcompound` )
    REFERENCES `compound` (`idcompound` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `pathway_has_enzyme`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pathway_has_enzyme` ;

CREATE  TABLE IF NOT EXISTS `pathway_has_enzyme` (
  `pathway_idpathway` INT UNSIGNED NOT NULL ,
  `enzyme_ecnumber` VARCHAR(15) NOT NULL ,
  `enzyme_protein_idprotein` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`pathway_idpathway`, `enzyme_ecnumber`, `enzyme_protein_idprotein`) ,
  CONSTRAINT `fk_pathway_has_enzyme_pathway1`
    FOREIGN KEY (`pathway_idpathway` )
    REFERENCES `pathway` (`idpathway` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_pathway_has_enzyme_enzyme1`
    FOREIGN KEY (`enzyme_ecnumber` , `enzyme_protein_idprotein` )
    REFERENCES `enzyme` (`ecnumber` , `protein_idprotein` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `gene_has_compartment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `gene_has_compartment` ;

CREATE  TABLE IF NOT EXISTS `gene_has_compartment` (
  `gene_idgene` INT UNSIGNED NOT NULL ,
  `compartment_idcompartment` INT UNSIGNED NOT NULL ,
  `primaryLocation` TINYINT(1) NULL ,
  `score` VARCHAR(10) NULL ,
  PRIMARY KEY (`gene_idgene`, `compartment_idcompartment`) ,
  CONSTRAINT `fk_gene_has_compartment_gene1`
    FOREIGN KEY (`gene_idgene` )
    REFERENCES `gene` (`idgene` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_gene_has_compartment_compartment1`
    FOREIGN KEY (`compartment_idcompartment` )
    REFERENCES `compartment` (`idcompartment` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `modules_has_compound`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `modules_has_compound` ;

CREATE  TABLE IF NOT EXISTS `modules_has_compound` (
  `modules_id` INT NOT NULL ,
  `compound_idcompound` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`modules_id`, `compound_idcompound`) ,
  CONSTRAINT `fk_modules_has_compound_modules1`
    FOREIGN KEY (`modules_id` )
    REFERENCES `module` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_modules_has_compound_compound1`
    FOREIGN KEY (`compound_idcompound` )
    REFERENCES `compound` (`idcompound` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `orthology`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `orthology` ;

CREATE  TABLE IF NOT EXISTS `orthology` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `entry_id` VARCHAR(10) NOT NULL ,
  `locus_id` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `module_has_orthology`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `module_has_orthology` ;

CREATE  TABLE IF NOT EXISTS `module_has_orthology` (
  `module_id` INT NOT NULL ,
  `orthology_id` INT NOT NULL ,
  PRIMARY KEY (`module_id`, `orthology_id`) ,
  CONSTRAINT `fk_modules_has_orthology_modules1`
    FOREIGN KEY (`module_id` )
    REFERENCES `module` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_modules_has_orthology_orthology1`
    FOREIGN KEY (`orthology_id` )
    REFERENCES `orthology` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `pathway_has_module`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pathway_has_module` ;

CREATE  TABLE IF NOT EXISTS `pathway_has_module` (
  `pathway_idpathway` INT UNSIGNED NOT NULL ,
  `module_id` INT NOT NULL ,
  PRIMARY KEY (`pathway_idpathway`, `module_id`) ,
  CONSTRAINT `fk_pathway_has_modules_pathway1`
    FOREIGN KEY (`pathway_idpathway` )
    REFERENCES `pathway` (`idpathway` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_pathway_has_modules_modules1`
    FOREIGN KEY (`module_id` )
    REFERENCES `module` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `gene_has_orthology`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `gene_has_orthology` ;

CREATE  TABLE IF NOT EXISTS `gene_has_orthology` (
  `gene_idgene` INT UNSIGNED NOT NULL ,
  `orthology_id` INT NOT NULL ,
  `similarity` FLOAT NULL ,
  PRIMARY KEY (`gene_idgene`, `orthology_id`) ,
  CONSTRAINT `fk_gene_has_orthology_gene1`
    FOREIGN KEY (`gene_idgene` )
    REFERENCES `gene` (`idgene` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_gene_has_orthology_orthology1`
    FOREIGN KEY (`orthology_id` )
    REFERENCES `orthology` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `same_as`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `same_as` ;

CREATE  TABLE IF NOT EXISTS `same_as` (
  `metabolite_id` INT UNSIGNED NOT NULL ,
  `similar_metabolite_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`metabolite_id`, `similar_metabolite_id`) ,
  CONSTRAINT `fk_same_as_metabolite_id`
    FOREIGN KEY (`metabolite_id` )
    REFERENCES `compound` (`idcompound` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_same_as_similar_metabolite_id`
    FOREIGN KEY (`similar_metabolite_id` )
    REFERENCES `compound` (`idcompound` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `is_super_reaction`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `is_super_reaction` ;

CREATE  TABLE IF NOT EXISTS `is_super_reaction` (
  `super_reaction_id` INT UNSIGNED NOT NULL ,
  `sub_reaction_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`super_reaction_id`, `sub_reaction_id`) ,
  CONSTRAINT `fk_is_super_reaction_super_reaction_id`
    FOREIGN KEY (`super_reaction_id` )
    REFERENCES `reaction` (`idreaction` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_is_super_reaction_sub_reaction_id`
    FOREIGN KEY (`sub_reaction_id` )
    REFERENCES `reaction` (`idreaction` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `superpathway`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `superpathway` ;

CREATE  TABLE IF NOT EXISTS `superpathway` (
  `pathway_idpathway` INT UNSIGNED NOT NULL ,
  `superpathway` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`pathway_idpathway`, `superpathway`) ,
  CONSTRAINT `fk_pathway_has_pathway_pathway1`
    FOREIGN KEY (`pathway_idpathway` )
    REFERENCES `pathway` (`idpathway` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_pathway_has_pathway_pathway2`
    FOREIGN KEY (`superpathway` )
    REFERENCES `pathway` (`idpathway` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Placeholder table for view `reactions_view_noPath_or_noEC`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `reactions_view_noPath_or_noEC` (`idreaction` INT, `reaction_name` INT, `equation` INT, `reversible` INT, `idpathway` INT, `pathway_name` INT, `inModel` INT, `isGeneric` INT, `source` INT, `originalReaction` INT, `compartment_idcompartment` INT, `notes` INT);

-- -----------------------------------------------------
-- Placeholder table for view `reactions_view`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `reactions_view` (`idreaction` INT, `reaction_name` INT, `equation` INT, `reversible` INT, `idpathway` INT, `pathway_name` INT, `inModel` INT, `isGeneric` INT, `source` INT, `originalReaction` INT, `compartment_idcompartment` INT, `notes` INT);

-- -----------------------------------------------------
-- View `reactions_view_noPath_or_noEC`
-- -----------------------------------------------------
DROP VIEW IF EXISTS `reactions_view_noPath_or_noEC` ;
DROP TABLE IF EXISTS `reactions_view_noPath_or_noEC`;
CREATE  OR REPLACE VIEW reactions_view_noPath_or_noEC AS
SELECT DISTINCT idreaction, reaction.name AS reaction_name , equation, reversible, pathway.idpathway, pathway.name AS pathway_name , inModel, isGeneric, reaction.source, originalReaction, compartment_idcompartment , notes
FROM reaction
LEFT JOIN pathway_has_reaction ON idreaction=pathway_has_reaction.reaction_idreaction
LEFT JOIN pathway ON pathway.idpathway=pathway_has_reaction.pathway_idpathway
WHERE (idpathway IS NULL ) 
ORDER BY pathway.name,  reaction.name
;

-- -----------------------------------------------------
-- View `reactions_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS `reactions_view` ;
DROP TABLE IF EXISTS `reactions_view`;
CREATE  OR REPLACE VIEW reactions_view AS
SELECT DISTINCT idreaction, reaction.name AS reaction_name, equation, reversible, pathway.idpathway, pathway.name AS pathway_name, reaction.inModel, isGeneric, reaction.source, originalReaction, compartment_idcompartment, notes
FROM reaction
LEFT JOIN pathway_has_reaction ON idreaction=pathway_has_reaction.reaction_idreaction
LEFT JOIN pathway ON pathway.idpathway=pathway_has_reaction.pathway_idpathway
WHERE pathway_has_reaction.pathway_idpathway=pathway.idpathway
ORDER BY pathway.name, reaction.name;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
