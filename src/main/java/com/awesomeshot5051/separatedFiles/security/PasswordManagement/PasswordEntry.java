package com.awesomeshot5051.separatedFiles.security.PasswordManagement;

import javafx.beans.property.*;

public class PasswordEntry {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty website = new SimpleStringProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();
    private final StringProperty label = new SimpleStringProperty();
    private final StringProperty notes = new SimpleStringProperty();
    private final StringProperty strength = new SimpleStringProperty();
    private final StringProperty lastAccessed = new SimpleStringProperty();
    private final BooleanProperty newEntry = new SimpleBooleanProperty(false);
    private final BooleanProperty edited = new SimpleBooleanProperty(false);

    // Per-field edit tracking
    private final BooleanProperty websiteEdited = new SimpleBooleanProperty(false);
    private final BooleanProperty usernameEdited = new SimpleBooleanProperty(false);
    private final BooleanProperty passwordEdited = new SimpleBooleanProperty(false);
    private final BooleanProperty labelEdited = new SimpleBooleanProperty(false);
    private final BooleanProperty notesEdited = new SimpleBooleanProperty(false);
    private final BooleanProperty strengthEdited = new SimpleBooleanProperty(false);
    private final BooleanProperty lastAccessedEdited = new SimpleBooleanProperty(false);

    public PasswordEntry() {
        this("", "", "", "", "", "", "", true);
    }

    public PasswordEntry(String website, String username, String password, String label,
                         String notes, String strength, String lastAccessed, Boolean newEntry) {
        this.website.set(website);
        this.username.set(username);
        this.password.set(password);
        this.label.set(label);
        this.notes.set(notes);
        this.strength.set(strength);
        this.lastAccessed.set(lastAccessed);
        this.newEntry.set(newEntry);
    }

    public PasswordEntry(int id, String website, String username, String password, String label,
                         String notes, String strength, String lastAccessed) {
        this.id.set(id);
        this.website.set(website);
        this.username.set(username);
        this.password.set(password);
        this.label.set(label);
        this.notes.set(notes);
        this.strength.set(strength);
        this.lastAccessed.set(lastAccessed);
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public boolean isEdited() {
        return edited.get();
    }

    public void setEdited(Boolean value) {
        edited.set(value);
    }

    public boolean isNewEntry() {
        return newEntry.get();
    }

    public void setNewEntry(boolean value) {
        newEntry.set(value);
    }

    public String getWebsite() {
        return website.get();
    }

    public void setWebsite(String v) {
        website.set(v);
    }

    public StringProperty websiteProperty() {
        return website;
    }

    public boolean isWebsiteEdited() {
        return websiteEdited.get();
    }

    public void setWebsiteEdited(boolean value) {
        websiteEdited.set(value);
    }

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String v) {
        username.set(v);
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public boolean isUsernameEdited() {
        return usernameEdited.get();
    }

    public void setUsernameEdited(boolean value) {
        usernameEdited.set(value);
    }

    public String getPassword() {
        return password.get();
    }

    public void setPassword(String v) {
        password.set(v);
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public boolean isPasswordEdited() {
        return passwordEdited.get();
    }

    public void setPasswordEdited(boolean value) {
        passwordEdited.set(value);
    }

    public String getLabel() {
        return label.get();
    }

    public void setLabel(String v) {
        label.set(v);
    }

    public StringProperty labelProperty() {
        return label;
    }

    public boolean isLabelEdited() {
        return labelEdited.get();
    }

    public void setLabelEdited(boolean value) {
        labelEdited.set(value);
    }

    public String getNotes() {
        return notes.get();
    }

    public void setNotes(String v) {
        notes.set(v);
    }

    public StringProperty notesProperty() {
        return notes;
    }

    public boolean isNotesEdited() {
        return notesEdited.get();
    }

    public void setNotesEdited(boolean value) {
        notesEdited.set(value);
    }

    public String getStrength() {
        return strength.get();
    }

    public void setStrength(String v) {
        strength.set(v);
    }

    public StringProperty strengthProperty() {
        return strength;
    }

    public boolean isStrengthEdited() {
        return strengthEdited.get();
    }

    public void setStrengthEdited(boolean value) {
        strengthEdited.set(value);
    }

    public String getLastAccessed() {
        return lastAccessed.get();
    }

    public void setLastAccessed(String v) {
        lastAccessed.set(v);
    }

    public StringProperty lastAccessedProperty() {
        return lastAccessed;
    }

    public boolean isLastAccessedEdited() {
        return lastAccessedEdited.get();
    }

    public void setLastAccessedEdited(boolean value) {
        lastAccessedEdited.set(value);
    }
}
