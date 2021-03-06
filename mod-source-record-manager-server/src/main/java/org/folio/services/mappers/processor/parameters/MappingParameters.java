package org.folio.services.mappers.processor.parameters;

import org.apache.commons.collections4.list.UnmodifiableList;
import org.folio.rest.jaxrs.model.AlternativeTitleType;
import org.folio.rest.jaxrs.model.ClassificationType;
import org.folio.rest.jaxrs.model.ContributorNameType;
import org.folio.rest.jaxrs.model.ElectronicAccessRelationship;
import org.folio.rest.jaxrs.model.ContributorType;
import org.folio.rest.jaxrs.model.IdentifierType;
import org.folio.rest.jaxrs.model.InstanceFormat;
import org.folio.rest.jaxrs.model.InstanceNoteType;
import org.folio.rest.jaxrs.model.InstanceType;
import org.folio.rest.jaxrs.model.IssuanceMode;

import java.util.List;

/**
 * Class to store parameters needed for mapping functions
 */
public class MappingParameters {

  private boolean initializedState = false;
  private UnmodifiableList<IdentifierType> identifierTypes;
  private UnmodifiableList<ClassificationType> classificationTypes;
  private UnmodifiableList<InstanceType> instanceTypes;
  private UnmodifiableList<ElectronicAccessRelationship> electronicAccessRelationship;
  private UnmodifiableList<InstanceFormat> instanceFormats;
  private UnmodifiableList<ContributorType> contributorTypes;
  private UnmodifiableList<ContributorNameType> contributorNameTypes;
  private UnmodifiableList<InstanceNoteType> instanceNoteTypes;
  private UnmodifiableList<AlternativeTitleType> alternativeTitleTypes;
  private UnmodifiableList<IssuanceMode> issuanceModes;

  public boolean isInitialized() {
    return initializedState;
  }

  public MappingParameters withInitializedState(boolean initializedState) {
    this.initializedState = initializedState;
    return this;
  }

  public List<IdentifierType> getIdentifierTypes() {
    return identifierTypes;
  }

  public MappingParameters withIdentifierTypes(List<IdentifierType> identifierTypes) {
    this.identifierTypes = new UnmodifiableList<>(identifierTypes);
    return this;
  }

  public List<ClassificationType> getClassificationTypes() {
    return classificationTypes;
  }

  public MappingParameters withClassificationTypes(List<ClassificationType> classificationTypes) {
    this.classificationTypes = new UnmodifiableList<>(classificationTypes);
    return this;
  }

  public List<InstanceType> getInstanceTypes() {
    return instanceTypes;
  }

  public MappingParameters withInstanceTypes(List<InstanceType> instanceTypes) {
    this.instanceTypes = new UnmodifiableList<>(instanceTypes);
    return this;
  }

  public List<ElectronicAccessRelationship> getElectronicAccessRelationships() {
    return electronicAccessRelationship;
  }

  public MappingParameters withElectronicAccessRelationships(List<ElectronicAccessRelationship> electronicAccessRelationship) {
    this.electronicAccessRelationship = new UnmodifiableList<>(electronicAccessRelationship);
    return this;
  }

  public List<InstanceFormat> getInstanceFormats() {
    return instanceFormats;
  }

  public MappingParameters withInstanceFormats(List<InstanceFormat> instanceFormats) {
    this.instanceFormats = new UnmodifiableList<>(instanceFormats);
    return this;
  }

  public List<ContributorType> getContributorTypes() {
    return contributorTypes;
  }

  public MappingParameters withContributorTypes(List<ContributorType> contributorTypes) {
    this.contributorTypes = new UnmodifiableList<>(contributorTypes);
    return this;
  }

  public List<ContributorNameType> getContributorNameTypes() {
    return contributorNameTypes;
  }

  public MappingParameters withContributorNameTypes(List<ContributorNameType> contributorNameTypes) {
    this.contributorNameTypes = new UnmodifiableList<>(contributorNameTypes);
    return this;
  }

  public List<InstanceNoteType> getInstanceNoteTypes() {
    return instanceNoteTypes;
  }

  public MappingParameters withInstanceNoteTypes(List<InstanceNoteType> instanceNoteTypes) {
    this.instanceNoteTypes = new UnmodifiableList<>(instanceNoteTypes);
    return this;
  }

  public List<AlternativeTitleType> getAlternativeTitleTypes() {
    return alternativeTitleTypes;
  }

  public MappingParameters withAlternativeTitleTypes(List<AlternativeTitleType> alternativeTitleTypes) {
    this.alternativeTitleTypes = new UnmodifiableList<>(alternativeTitleTypes);
    return this;
  }

  public UnmodifiableList<IssuanceMode> getIssuanceModes() {
    return issuanceModes;
  }

  public MappingParameters withIssuanceModes(List<IssuanceMode> issuanceModes) {
    this.issuanceModes = new UnmodifiableList<>(issuanceModes);
    return this;
  }
}
