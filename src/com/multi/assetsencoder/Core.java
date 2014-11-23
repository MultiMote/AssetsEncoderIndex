package com.multi.assetsencoder;

import java.io.*;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Core {

    public static void doWork(String jsonfile, String dir, String outputDir) throws IOException { //говнокод
        System.out.println("Started.");

        BufferedWriter writer;
        File assetsDir = new File(dir);
        List<File> fileList;


        if (assetsDir.isDirectory()) {
            fileList = getFileListRecursive(assetsDir);
        }else {
            System.out.println("Assets dir is not dir or not exists =O");
            return;
        }

        System.out.println("Creating " + outputDir + "/indexes/" +  jsonfile );
        File json = new File(outputDir + File.separator + "indexes");
        json.mkdirs();
        writer = new BufferedWriter(new FileWriter(new File(outputDir + File.separator + "indexes",  jsonfile)));

        writer.write("{");
        writer.newLine();

        writer.write("  \"objects\": {");
        writer.newLine();

        int num = 0;
        for (File f : fileList) {
            if (f.exists()) {
                String hash = getMD5(f);
                String path = getRelativePath(f, assetsDir, true);
                String hashDir = hash.substring(0, 2);

                File hashOutput = new File(outputDir + File.separator + "objects", hashDir + File.separator + hash);
                File hashOutputDir = new File(outputDir + File.separator + "objects", hashDir);

                System.out.println("[File " + (num+1) + " of " + fileList.size() + "] - " + f.getName() + ", hash is " + hash + ", size is " + f.length() + " putting in objects/" + hashDir + " dir");
                writeObject(writer, path, hash, f.length(), num<fileList.size()-1);

                hashOutputDir.mkdirs();
                copyFile(f, hashOutput);
            }
            num++;
        }

        writer.write("  }");
        writer.newLine();

        writer.write("}");

        writer.close();

        System.out.println("Finished.");
    }

    public static void writeObject(BufferedWriter writer, String path, String hash, long size, boolean cont) throws IOException{
        writer.write("    \""+ path +"\": {");
        writer.newLine();
        writer.write("      \"hash\": \"" + hash + "\",");
        writer.newLine();
        writer.write("      \"size\": " + size);
        writer.newLine();
        writer.write("    }");
        if(cont)writer.write(",");
        writer.newLine();

    }

    public static String getMD5(File file) {
        String s = null;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA1");
            InputStream is = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0)
                md.update(buffer, 0, read);
            byte[] md5 = md.digest();
            BigInteger bi = new BigInteger(1, md5);
            s = bi.toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static String getRelativePath(File file, File folder, boolean slashes) {
        String filePath = file.getAbsolutePath();
        String folderPath = folder.getAbsolutePath();
        if (filePath.startsWith(folderPath)) {
            String s = filePath.substring(folderPath.length() + 1);
            if (slashes) s = s.replace('\\', '/');
            return s;
        } else {
            return null;
        }
    }

    public static List<File> getFileListRecursive(File dir) {
        List<File> list = new ArrayList<File>();
        File[] files = dir.listFiles();

        if (files != null)
            for (File file : files) {

                if (file.isDirectory()) {
                    list.addAll(getFileListRecursive(file));
                } else list.add(file);
            }

        return list;
    }


    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: outputJsonFile assetsDir outputDir");
            System.out.println("Usage example: java -jar AssetsEncoder.jar 1.7.10.json assets converted");
            System.out.println("THIS JAR MUST BE IN SAME PATH AS ASSETS DIR!");
            System.out.println("ASSETS DIR MUST CONTAIN \"minecraft\" DIR (WITH \"sounds\", \"lang\" AND OTHER DIRS) AND \"pack.mcmeta\" FILE!");
            return;
        }
        try {
            doWork(args[0], args[1], args[2]);
           // doWork("1.7.10.json", "assets", "converted");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
