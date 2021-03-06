package api.support.fixtures;

import java.util.UUID;

import api.support.http.IndividualResource;

import api.support.builders.NoteTypeBuilder;
import api.support.http.ResourceClient;

public class NoteTypeFixture extends RecordCreator {

  public NoteTypeFixture() {
    super(ResourceClient.forNoteTypes(), json -> json.getString("noteTypes"));
  }

  public IndividualResource generalNoteType() {
    return createIfAbsent(new NoteTypeBuilder()
      .withId(UUID.randomUUID())
      .withTypeName("General note"));
  }
}
