package org.folio.services.parsers;

/**
 * Common interface for Raw Record Parser. Parsers for each format should implement it
 */
public interface RawRecordParser {

  /**
   * Method parse Raw record and return String with json representation of record
   *
   * @param rawRecord - String with raw record
   * @return - Wrapper for parsed record in json format.
   * Can contains errors descriptions if parsing was failed
   */
  ParsedResult parseRecord(String rawRecord);

  /**
   * @return - format which RecordParser can parse
   */
  RecordFormat getParserFormat();
}