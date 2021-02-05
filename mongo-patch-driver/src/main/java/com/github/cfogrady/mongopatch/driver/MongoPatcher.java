package com.github.cfogrady.mongopatch.driver;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.fasterxml.jackson.databind.JavaType;
import com.github.cfogrady.mongopatch.core.SchemaBuilder;
import com.github.cfogrady.mongopatch.core.operations.PatchOperation;
import com.github.cfogrady.mongopatch.core.schema.verifiers.PatchVerifierContainer;

/**
 * Generates an Update statement for a list of patch operations
 */
public class MongoPatcher {

    private final Map<String, JavaType> schema;
    private final PatchVerifierContainer patchVerifierContainer;

    public MongoPatcher(Class<?> schemaConstraint) {
        this(schemaConstraint, PatchVerifierContainer.defaultInstance);
    }

    public MongoPatcher(Class<?> schemaConstraint, PatchVerifierContainer patchVerifierContainer) {
        if (schemaConstraint != null) {
            schema = SchemaBuilder.buildSchema(schemaConstraint);
        } else {
            schema = null;
        }
        if (patchVerifierContainer == null) {
            throw new NullPointerException("PatchVerifierFactory null in MongoPatcher constructor");
        }
        this.patchVerifierContainer = patchVerifierContainer; 
    }

    public Bson convertToUpdate(List<PatchOperation> patch, Object originalEntity) {
        return convertToUpdate(patch, originalEntity, schema != null);
    }

    public Bson convertToUpdate(List<PatchOperation> patch, Object originalEntity,
        boolean constrainBySchema) {
        if (constrainBySchema) {
            verifyAgainstSchema(patch, originalEntity);
        }
        Document update = new Document();
        return update;
    }

    public void verifyAgainstSchema(List<PatchOperation> patch, Object originalEntity) {
        if (schema == null) {
            throw new UnsupportedOperationException(
                    "Cannot verify against a schema if none have been specified");
        }
        if (originalEntity == null) {
            throw new IllegalArgumentException("OriginalEntity must be present for test verification");
        }
        for (PatchOperation operation : patch) {
            patchVerifierContainer.getPatchVerifier(operation.getOperationType()).verify(operation, schema);

        }

    }
}
