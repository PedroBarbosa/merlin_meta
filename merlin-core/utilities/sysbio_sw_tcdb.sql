SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';


-- -----------------------------------------------------
-- Table `sw_reports`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sw_reports` ;

CREATE  TABLE IF NOT EXISTS `sw_reports` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `project_id` INT NOT NULL ,
  `locus_tag` VARCHAR(45) NOT NULL ,
  `date` TIMESTAMP NOT NULL ,
  `matrix` VARCHAR(45) NULL ,
  `number_TMD` INT(11) NOT NULL ,
  `status` ENUM('PROCESSED','PROCESSING') NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `locus_tag` (`locus_tag` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `sw_hits`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sw_hits` ;

CREATE  TABLE IF NOT EXISTS `sw_hits` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `acc` VARCHAR(45) NOT NULL ,
  `tcdb_id` VARCHAR(45) NOT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `sw_similarities`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sw_similarities` ;

CREATE  TABLE IF NOT EXISTS `sw_similarities` (
  `sw_report_id` INT NOT NULL ,
  `sw_hit_id` INT NOT NULL ,
  `similarity` FLOAT NULL ,
  INDEX `fk_sw_reports_has_sw_hits_sw_hits_idx` (`sw_hit_id` ASC) ,
  INDEX `fk_sw_reports_has_sw_hits_sw_reports_idx` (`sw_report_id` ASC) ,
  PRIMARY KEY (`sw_report_id`, `sw_hit_id`) ,
  CONSTRAINT `fk_sw_reports_has_sw_hits_sw_reports`
    FOREIGN KEY (`sw_report_id` )
    REFERENCES `sw_reports` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_sw_reports_has_sw_hits_sw_hits`
    FOREIGN KEY (`sw_hit_id` )
    REFERENCES `sw_hits` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Placeholder table for view `sw_transporters`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `sw_transporters` (`locus_tag` INT, `acc` INT, `tcdb_id` INT, `similarity` INT, `project_id` INT);

-- -----------------------------------------------------
-- View `sw_transporters`
-- -----------------------------------------------------
DROP VIEW IF EXISTS `sw_transporters` ;
DROP TABLE IF EXISTS `sw_transporters`;
CREATE  OR REPLACE VIEW sw_transporters AS SELECT locus_tag, acc, tcdb_id, similarity, project_id FROM sw_reports
INNER JOIN sw_similarities ON sw_reports.id = sw_similarities.sw_report_id
INNER JOIN sw_hits ON sw_hits.id = sw_similarities.sw_hit_id;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
