SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';


-- -----------------------------------------------------
-- Table `projects`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `projects` ;

CREATE  TABLE IF NOT EXISTS `projects` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `organism_id` INT NULL ,
  `latest_version` TINYINT(1) NULL ,
  `date` TIMESTAMP NULL ,
  `version` INT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `psort_reports`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `psort_reports` ;

CREATE  TABLE IF NOT EXISTS `psort_reports` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `project_id` INT NOT NULL ,
  `locus_tag` VARCHAR(45) NULL ,
  `date` VARCHAR(45) NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_psort_reports_projects1_idx` (`project_id` ASC) ,
  CONSTRAINT `fk_psort_reports_projects1`
    FOREIGN KEY (`project_id` )
    REFERENCES `projects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `compartments`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `compartments` ;

CREATE  TABLE IF NOT EXISTS `compartments` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(45) NULL ,
  `abbreviation` VARCHAR(10) NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `psort_reports_has_compartments`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `psort_reports_has_compartments` ;

CREATE  TABLE IF NOT EXISTS `psort_reports_has_compartments` (
  `psort_report_id` INT NOT NULL ,
  `compartment_id` INT NOT NULL ,
  `score` FLOAT NULL ,
  PRIMARY KEY (`psort_report_id`, `compartment_id`) ,
  INDEX `fk_psort_reports_has_compartments_compartments1_idx` (`compartment_id` ASC) ,
  INDEX `fk_psort_reports_has_compartments_psort_reports_idx` (`psort_report_id` ASC) ,
  CONSTRAINT `fk_psort_reports_has_compartments_psort_reports`
    FOREIGN KEY (`psort_report_id` )
    REFERENCES `psort_reports` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_psort_reports_has_compartments_compartments1`
    FOREIGN KEY (`compartment_id` )
    REFERENCES `compartments` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
