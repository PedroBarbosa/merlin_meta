SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';


-- -----------------------------------------------------
-- Table `homologySetup`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `homologySetup` ;

CREATE  TABLE IF NOT EXISTS `homologySetup` (
  `s_key` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `program` VARCHAR(45) NULL ,
  `version` VARCHAR(255) NULL ,
  `databaseID` VARCHAR(255) NULL ,
  `eValue` VARCHAR(45) NULL ,
  `matrix` VARCHAR(45) NULL ,
  `wordSize` VARCHAR(5) NULL ,
  `gapCosts` VARCHAR(15) NULL ,
  `maxNumberOfAlignments` INT NULL ,
  PRIMARY KEY (`s_key`) )
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `geneHomology`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `geneHomology` ;

CREATE  TABLE IF NOT EXISTS `geneHomology` (
  `s_key` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `homologySetup_s_key` INT UNSIGNED NOT NULL ,
  `locusTag` VARCHAR(40) NULL ,
  `query` VARCHAR(45) NULL ,
  `gene` VARCHAR(45) NULL ,
  `chromosome` VARCHAR(20) NULL ,
  `organelle` VARCHAR(45) NULL ,
  `uniprot_star` TINYINT(1) NULL ,
  `status` VARCHAR(45) NULL ,
  `uniprot_ecnumber` VARCHAR(150) NULL ,
  PRIMARY KEY (`s_key`) ,
  CONSTRAINT `fk_geneblast_blastSetup1`
    FOREIGN KEY (`homologySetup_s_key` )
    REFERENCES `homologySetup` (`s_key` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `fastaSequence`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `fastaSequence` ;

CREATE  TABLE IF NOT EXISTS `fastaSequence` (
  `s_key` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `geneHomology_s_key` INT UNSIGNED NOT NULL ,
  `sequence` TEXT NULL ,
  PRIMARY KEY (`s_key`, `geneHomology_s_key`) ,
  CONSTRAINT `fk_fastaSequence_geneblast1`
    FOREIGN KEY (`geneHomology_s_key` )
    REFERENCES `geneHomology` (`s_key` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `organism`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `organism` ;

CREATE  TABLE IF NOT EXISTS `organism` (
  `s_key` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `organism` VARCHAR(100) NULL ,
  `taxonomy` TEXT NULL ,
  `taxRank` INT NULL ,
  PRIMARY KEY (`s_key`) )
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `homologues`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `homologues` ;

CREATE  TABLE IF NOT EXISTS `homologues` (
  `s_key` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `organism_s_key` INT UNSIGNED NOT NULL ,
  `locusID` VARCHAR(40) NULL ,
  `definition` TEXT NULL ,
  `calculated_mw` FLOAT NULL ,
  `product` TEXT NULL ,
  `organelle` VARCHAR(45) NULL ,
  `uniprot_star` TINYINT(1) NULL ,
  PRIMARY KEY (`s_key`) ,
  CONSTRAINT `fk_homologyData_organism1`
    FOREIGN KEY (`organism_s_key` )
    REFERENCES `organism` (`s_key` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `ecNumber`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ecNumber` ;

CREATE  TABLE IF NOT EXISTS `ecNumber` (
  `s_key` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `ecNumber` MEDIUMTEXT NULL ,
  PRIMARY KEY (`s_key`) )
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `productRank`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `productRank` ;

CREATE  TABLE IF NOT EXISTS `productRank` (
  `s_key` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `geneHomology_s_key` INT UNSIGNED NOT NULL ,
  `productName` TEXT NULL ,
  `rank` INT UNSIGNED NULL ,
  PRIMARY KEY (`s_key`, `geneHomology_s_key`) ,
  CONSTRAINT `fk_productRank_geneblast1`
    FOREIGN KEY (`geneHomology_s_key` )
    REFERENCES `geneHomology` (`s_key` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `ecNumberRank`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ecNumberRank` ;

CREATE  TABLE IF NOT EXISTS `ecNumberRank` (
  `s_key` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `geneHomology_s_key` INT UNSIGNED NOT NULL ,
  `ecNumber` MEDIUMTEXT NULL ,
  `rank` INT UNSIGNED NULL ,
  PRIMARY KEY (`s_key`, `geneHomology_s_key`) ,
  CONSTRAINT `fk_ecNumberRank_geneblast1`
    FOREIGN KEY (`geneHomology_s_key` )
    REFERENCES `geneHomology` (`s_key` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
PACK_KEYS = 0
ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `ecNumberRank_has_organism`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ecNumberRank_has_organism` ;

CREATE  TABLE IF NOT EXISTS `ecNumberRank_has_organism` (
  `ecNumberRank_s_key` INT UNSIGNED NOT NULL ,
  `organism_s_key` INT UNSIGNED NOT NULL ,
  CONSTRAINT `fk_ecNumberRank_has_organism_ecNumberRank1`
    FOREIGN KEY (`ecNumberRank_s_key` )
    REFERENCES `ecNumberRank` (`s_key` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_ecNumberRank_has_organism_organism1`
    FOREIGN KEY (`organism_s_key` )
    REFERENCES `organism` (`s_key` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `productRank_has_organism`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `productRank_has_organism` ;

CREATE  TABLE IF NOT EXISTS `productRank_has_organism` (
  `productRank_s_key` INT UNSIGNED NOT NULL ,
  `organism_s_key` INT UNSIGNED NOT NULL ,
  CONSTRAINT `fk_productRank_has_organism_productRank1`
    FOREIGN KEY (`productRank_s_key` )
    REFERENCES `productRank` (`s_key` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_productRank_has_organism_organism1`
    FOREIGN KEY (`organism_s_key` )
    REFERENCES `organism` (`s_key` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `homologyData`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `homologyData` ;

CREATE  TABLE IF NOT EXISTS `homologyData` (
  `s_key` INT NOT NULL AUTO_INCREMENT ,
  `geneHomology_s_key` INT NOT NULL ,
  `locusTag` VARCHAR(45) NOT NULL ,
  `geneName` VARCHAR(45) NULL ,
  `product` TEXT NOT NULL ,
  `ecNumber` MEDIUMTEXT NULL ,
  `selected` TINYINT(1) NULL ,
  `chromosome` VARCHAR(20) NULL ,
  `notes` TEXT NULL ,
  PRIMARY KEY (`s_key`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `productList`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `productList` ;

CREATE  TABLE IF NOT EXISTS `productList` (
  `s_key` INT NOT NULL AUTO_INCREMENT ,
  `homologyData_s_key` INT NOT NULL ,
  `otherNames` TEXT NULL ,
  PRIMARY KEY (`s_key`) ,
  CONSTRAINT `fk_productList_blastData1`
    FOREIGN KEY (`homologyData_s_key` )
    REFERENCES `homologyData` (`s_key` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ecNumberList`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ecNumberList` ;

CREATE  TABLE IF NOT EXISTS `ecNumberList` (
  `s_key` INT NOT NULL AUTO_INCREMENT ,
  `homologyData_s_key` INT NOT NULL ,
  `otherECNumbers` MEDIUMTEXT NULL ,
  PRIMARY KEY (`s_key`) ,
  CONSTRAINT `fk_ecNumberList_blastData1`
    FOREIGN KEY (`homologyData_s_key` )
    REFERENCES `homologyData` (`s_key` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `geneHomology_has_homologues`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `geneHomology_has_homologues` ;

CREATE  TABLE IF NOT EXISTS `geneHomology_has_homologues` (
  `geneHomology_s_key` INT UNSIGNED NOT NULL ,
  `homologues_s_key` INT UNSIGNED NOT NULL ,
  `referenceID` VARCHAR(100) NULL ,
  `gene` VARCHAR(100) NULL ,
  `eValue` FLOAT NULL ,
  `bits` VARCHAR(100) NULL ,
  CONSTRAINT `fk_geneblast_has_homologues_geneblast1`
    FOREIGN KEY (`geneHomology_s_key` )
    REFERENCES `geneHomology` (`s_key` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_geneblast_has_homologues_homologues1`
    FOREIGN KEY (`homologues_s_key` )
    REFERENCES `homologues` (`s_key` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `homologues_has_ecNumber`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `homologues_has_ecNumber` ;

CREATE  TABLE IF NOT EXISTS `homologues_has_ecNumber` (
  `homologues_s_key` INT UNSIGNED NOT NULL ,
  `ecNumber_s_key` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`homologues_s_key`, `ecNumber_s_key`) ,
  CONSTRAINT `fk_homologues_has_ecNumber_homologues1`
    FOREIGN KEY (`homologues_s_key` )
    REFERENCES `homologues` (`s_key` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_homologues_has_ecNumber_ecNumber1`
    FOREIGN KEY (`ecNumber_s_key` )
    REFERENCES `ecNumber` (`s_key` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;