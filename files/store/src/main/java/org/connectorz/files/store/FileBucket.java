/*
Copyright 2012 Adam Bien, adam-bien.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.connectorz.files.store;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import org.connectorz.files.Bucket;

public class FileBucket implements Bucket, LocalTransaction {

    private ConnectionRequestInfo connectionRequestInfo;
    private String rootDirectory;
    private ConcurrentHashMap<String, byte[]> txCache;
    private Set<String> deletedFiles;
    private GenericManagedConnection genericManagedConnection;
    private PrintWriter out;

    public FileBucket(PrintWriter out, String rootDirectory, GenericManagedConnection genericManagedConnection, ConnectionRequestInfo connectionRequestInfo) {
        this.out = out;
        this.rootDirectory = rootDirectory;
        out.println("#FileBucket " + connectionRequestInfo + " " + toString());
        this.genericManagedConnection = genericManagedConnection;
        this.connectionRequestInfo = connectionRequestInfo;
        this.txCache = new ConcurrentHashMap<>();
        this.deletedFiles = new ConcurrentSkipListSet<>();
    }

    private void initialize() {
        createIfNotExists(this.rootDirectory);
    }

    void createIfNotExists(String folderName) {
        File file = new File(folderName);
        if (file.exists() && file.isDirectory()) {
            return;
        }
        file.mkdirs();

    }

    @Override
    public void write(String fileName,byte[] content) {
        out.println("#FileBucket.write " + fileName + " " + content);
        txCache.put(fileName, content);
    }

    void close() {
        this.genericManagedConnection.close();
    }

    public void destroy() {
        out.println("#FileBucket.cleanup");
        this.txCache.clear();
    }

    @Override
    public void begin() throws ResourceException {
        out.println("#FileBucket.begin " + toString());
        this.initialize();
    }

    @Override
    public void commit() throws ResourceException {
        out.println("#FileBucket.commit " + toString());
        try {
            processDeletions();
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot delete files: " +ex ,ex);
        }
        flushChanges();
    }
  
    private void flushChanges() throws ResourceException {
        Set<Entry<String, byte[]>> txSet = this.txCache.entrySet();
        for (Entry<String, byte[]> entry : txSet) {
            String fileName = entry.getKey();
            byte[] value = entry.getValue();
            writeFile(fileName, value);
            this.txCache.remove(fileName);
        }
    }
    
    void processDeletions() throws IOException {
        for (String fileName : deletedFiles) {
            deleteFile(getAbsoluteName(fileName));
        }
    }

    void writeFile(String fileName,byte[] content) throws ResourceException{
        try (FileOutputStream fileOutputStream = new FileOutputStream(getAbsoluteName(fileName), true)) {
                fileOutputStream.write(content);
                fileOutputStream.flush();
        } catch (IOException ex) {
            throw new ResourceException(ex);
        } finally {
            this.close();
        }
    }  
    
    String getAbsoluteName(String fileName){
        return this.rootDirectory + fileName;
    }

    @Override
    public void rollback() throws ResourceException {
        out.println("#FileBucket.rollback  " + toString());
        this.txCache.clear();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileBucket other = (FileBucket) obj;
        if (this.connectionRequestInfo != other.connectionRequestInfo && (this.connectionRequestInfo == null || !this.connectionRequestInfo.equals(other.connectionRequestInfo))) {
            return false;
        }
        return true;
    }

    
    
    @Override
    public byte[] fetch(String file) {
        byte[] entry = this.txCache.get(file);
        if(entry==null){
            try {
                entry = readFromFile(getAbsoluteName(file));
            } catch (IOException ex) {
                throw new IllegalStateException("Cannot access file: " + getAbsoluteName(file),ex);
            }
        }
        return entry;
    }
    
    
    byte[] readFromFile(String fileName) throws IOException{
        Path file = Paths.get(fileName);
        if(!Files.exists(file, LinkOption.NOFOLLOW_LINKS)){
            return null;
        }
        return Files.readAllBytes(file);
    }

    void deleteFile(String absoluteName) throws IOException {
        Path file = Paths.get(absoluteName);
        if(!Files.exists(file, LinkOption.NOFOLLOW_LINKS))
            return;
        Files.deleteIfExists(file);
    }

    @Override
    public void delete(String file) {
        this.txCache.remove(file);
        this.deletedFiles.add(file);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.connectionRequestInfo != null ? this.connectionRequestInfo.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "FileBucket{" + "connectionRequestInfo=" + connectionRequestInfo + ", rootDirectory=" + rootDirectory + ", txCache=" + txCache + ", genericManagedConnection=" + genericManagedConnection + '}';
    }

}
