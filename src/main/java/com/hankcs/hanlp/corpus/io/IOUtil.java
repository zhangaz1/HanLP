/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/9/8 23:04</create-date>
 *
 * <copyright file="Util.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.corpus.io;


import com.hankcs.hanlp.utility.TextUtility;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.*;

import static com.hankcs.hanlp.utility.Predefine.logger;

/**
 * 一些常用的IO操作
 *
 * @author hankcs
 */
public class IOUtil
{
    /**
     * 序列化对象
     *
     * @param o
     * @param path
     * @return
     */
    public static boolean saveObjectTo(Object o, String path)
    {
        try
        {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
            oos.writeObject(o);
            oos.close();
        }
        catch (IOException e)
        {
            logger.warning("在保存对象" + o + "到" + path + "时发生异常" + e);
            return false;
        }

        return true;
    }

    /**
     * 反序列化对象
     *
     * @param path
     * @return
     */
    public static Object readObjectFrom(String path)
    {
        ObjectInputStream ois = null;
        try
        {
            ois = new ObjectInputStream(new FileInputStream(path));
            Object o = ois.readObject();
            ois.close();
            return o;
        }
        catch (Exception e)
        {
            logger.warning("在从" + path + "读取对象时发生异常" + e);
        }

        return null;
    }

    /**
     * 一次性读入纯文本
     *
     * @param path
     * @return
     */
    public static String readTxt(String path)
    {
        if (path == null) return null;
        File file = new File(path);
        Long fileLength = file.length();
        byte[] fileContent = new byte[fileLength.intValue()];
        try
        {
            FileInputStream in = new FileInputStream(file);
            in.read(fileContent);
            in.close();
        }
        catch (FileNotFoundException e)
        {
            logger.warning("找不到" + path + e);
            return null;
        }
        catch (IOException e)
        {
            logger.warning("读取" + path + "发生IO异常" + e);
            return null;
        }

        return new String(fileContent, Charset.forName("UTF-8"));
    }

    public static LinkedList<String[]> readCsv(String path)
    {
        LinkedList<String[]> resultList = new LinkedList<String[]>();
        LinkedList<String> lineList = readLineList(path);
        for (String line : lineList)
        {
            resultList.add(line.split(","));
        }
        return resultList;
    }

    /**
     * 快速保存
     *
     * @param path
     * @param content
     * @return
     */
    public static boolean saveTxt(String path, String content)
    {
        try
        {
            FileChannel fc = new FileOutputStream(path).getChannel();
            fc.write(ByteBuffer.wrap(content.getBytes()));
            fc.close();
        }
        catch (Exception e)
        {
            logger.throwing("IOUtil", "saveTxt", e);
            logger.warning("IOUtil saveTxt 到" + path + "失败" + e.toString());
            return false;
        }
        return true;
    }

    public static boolean saveTxt(String path, StringBuilder content)
    {
        return saveTxt(path, content.toString());
    }

    public static <T> boolean saveCollectionToTxt(Collection<T> collection, String path)
    {
        StringBuilder sb = new StringBuilder();
        for (Object o : collection)
        {
            sb.append(o);
            sb.append('\n');
        }
        return saveTxt(path, sb.toString());
    }

    /**
     * 将整个文件读取为字节数组
     *
     * @param path
     * @return
     */
    public static byte[] readBytes(String path)
    {
        try
        {
            if (isResource(path)) return readBytesFromResource(path);
            FileInputStream fis = new FileInputStream(path);
            FileChannel channel = fis.getChannel();
            int fileSize = (int) channel.size();
            ByteBuffer byteBuffer = ByteBuffer.allocate(fileSize);
            channel.read(byteBuffer);
            byteBuffer.flip();
            byte[] bytes = byteBuffer.array();
            byteBuffer.clear();
            channel.close();
            fis.close();
            return bytes;
        }
        catch (Exception e)
        {
            logger.warning("读取" + path + "时发生异常" + e);
        }

        return null;
    }

    /**
     * 将资源中的一个资源读入byte数组
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static byte[] readBytesFromResource(String path) throws IOException
    {
        InputStream is = IOUtil.class.getResourceAsStream("/" + path);
        byte[] targetArray = new byte[is.available()];
        int len;
        int off = 0;
        while ((len = is.read(targetArray, off, targetArray.length - off)) != -1 && off < targetArray.length)
        {
            off += len;
        }
        is.close();
        return targetArray;
    }

    public static byte[] getBytes(InputStream is) throws IOException
    {

        int len;
        int size = 1024;
        byte[] buf;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        buf = new byte[size];
        while ((len = is.read(buf, 0, size)) != -1)
            bos.write(buf, 0, len);
        buf = bos.toByteArray();
        return buf;
    }

    public static LinkedList<String> readLineList(String path)
    {
        LinkedList<String> result = new LinkedList<String>();
        String txt = readTxt(path);
        if (txt == null) return result;
        StringTokenizer tokenizer = new StringTokenizer(txt, "\n");
        while (tokenizer.hasMoreTokens())
        {
            result.add(tokenizer.nextToken());
        }

        return result;
    }

    /**
     * 用省内存的方式读取大文件
     *
     * @param path
     * @return
     */
    public static LinkedList<String> readLineListWithLessMemory(String path)
    {
        LinkedList<String> result = new LinkedList<String>();
        String line = null;
        try
        {
            BufferedReader bw = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            while ((line = bw.readLine()) != null)
            {
                result.add(line);
            }
            bw.close();
        }
        catch (Exception e)
        {
            logger.warning("加载" + path + "失败，" + e);
        }

        return result;
    }

    public static boolean saveMapToTxt(Map<Object, Object> map, String path)
    {
        return saveMapToTxt(map, path, "=");
    }

    public static boolean saveMapToTxt(Map<Object, Object> map, String path, String separator)
    {
        map = new TreeMap<Object, Object>(map);
        return saveEntrySetToTxt(map.entrySet(), path, separator);
    }

    public static boolean saveEntrySetToTxt(Set<Map.Entry<Object, Object>> entrySet, String path, String separator)
    {
        StringBuilder sbOut = new StringBuilder();
        for (Map.Entry<Object, Object> entry : entrySet)
        {
            sbOut.append(entry.getKey());
            sbOut.append(separator);
            sbOut.append(entry.getValue());
            sbOut.append('\n');
        }
        return saveTxt(path, sbOut.toString());
    }

    public static LineIterator readLine(String path)
    {
        return new LineIterator(path);
    }

    /**
     * 方便读取按行读取大文件
     */
    public static class LineIterator implements Iterator<String>
    {
        BufferedReader bw;
        String line;

        public LineIterator(String path)
        {
            try
            {
                bw = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
                line = bw.readLine();
            }
            catch (FileNotFoundException e)
            {
                logger.warning("文件" + path + "不存在，接下来的调用会返回null" + TextUtility.exceptionToString(e));
            }
            catch (IOException e)
            {
                logger.warning("在读取过程中发生错误" + TextUtility.exceptionToString(e));
            }
        }

        public void close()
        {
            if (bw == null) return;
            try
            {
                bw.close();
                bw = null;
            }
            catch (IOException e)
            {
                logger.warning("关闭文件失败" + TextUtility.exceptionToString(e));
            }
            return;
        }

        @Override
        public boolean hasNext()
        {
            if (bw == null) return false;
            if (line == null)
            {
                try
                {
                    bw.close();
                    bw = null;
                }
                catch (IOException e)
                {
                    logger.warning("关闭文件失败" + TextUtility.exceptionToString(e));
                }
                return false;
            }

            return true;
        }

        @Override
        public String next()
        {
            String preLine = line;
            try
            {
                if (bw != null)
                {
                    line = bw.readLine();
                    if (line == null && bw != null)
                    {
                        try
                        {
                            bw.close();
                            bw = null;
                        }
                        catch (IOException e)
                        {
                            logger.warning("关闭文件失败" + TextUtility.exceptionToString(e));
                        }
                    }
                }
                else
                {
                    line = null;
                }
            }
            catch (IOException e)
            {
                logger.warning("在读取过程中发生错误" + TextUtility.exceptionToString(e));
            }
            return preLine;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("只读，不可写！");
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param path
     * @return
     */
    public static boolean isFileExists(String path)
    {
        return new File(path).exists();
    }

    /**
     * 判断资源是否位于jar中
     *
     * @param path
     * @return
     */
    public static boolean isResource(String path)
    {
        return path.startsWith("data/");   // 这样未必好，比如用户的root就叫/data/就会发生问题，不过目前就这么办了
    }

    /**
     * 智能获取InputStream，如果是资源文件则返回相应的InputStream
     *
     * @param path
     * @return
     * @throws FileNotFoundException
     */
    public static InputStream getInputStream(String path) throws FileNotFoundException
    {
        return isResource(path) ? IOUtil.class.getResourceAsStream("/" + path) : new FileInputStream(path);
    }
}
