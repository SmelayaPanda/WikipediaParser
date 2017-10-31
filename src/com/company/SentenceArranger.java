package com.company;

import com.company.support.AnsiColor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

public class SentenceArranger {

    private static String searchBreked = AnsiColor.RED + " Поиск прерван " + AnsiColor.RESET;

    public static void main( String[] args ) throws IOException {

        // Считываем все английские слова с ретингами в мапу
        Map<String, Integer> map = readFileToMap();
        // Сортируем мапу( позволит заново итерироваться с места падения при ошибках )
        Map<String, Integer> sortedMap = sortByValue( map );

        // Для какждого слова бежим по всей википедии по одному файлу в иттерации
        // Если нашли более x предложений - останавливаемся
        //            иначе сколько нашли столько нашли =) если уже всю вики прочесали

        // Записываем эти предложения построчно в файл в виде [ слово  ^^^ ранк предложения  ###  пример предложения ]

        // В папке /output должен появляться новый файлик каждые 1000 слов
        String fileForWrite = "src\\com\\company\\output\\engSentence_";

        String newFileForWrite;
        String wikiFile;
        String sentence;
        String[] splittedSentence;
        Date date;
        int sentenceCounter;
        int wordNum = -1;
        Map<String, Integer> sentencePull = new HashMap<>();
        Map<String, Integer> rankingSentencePull;

        BufferedWriter bw = null;
        try {
            for( Map.Entry<String, Integer> engWord : sortedMap.entrySet() ) {
                wordNum++;
                if( wordNum % 10 == 1 ) {
                    newFileForWrite = fileForWrite + "_" + ( wordNum + 9 ) + ".txt";
                    bw = createNewFile( newFileForWrite );
                }
                if( wordNum >= 0 && null != bw ) { // для случая падения на каком-то слове
                    date = new Date();
                    System.out.println( AnsiColor.BLUE +
                                        new Timestamp( date.getTime() ) +
                                        "\n Word №: " + wordNum + " " +
                                        "--> " + engWord.getKey() +
                                        AnsiColor.RESET );
                    sentenceCounter = 0;

                    search:
                    {
                        for( int i = 1; i < 4633; i++ ) {
                            if( searchBreakerIfFew( sentenceCounter, i ) ) {
                                break search;
                            }
                            wikiFile = iterateByWiki( i );
                            String[] sentenceArray = wikiFile.split( "\\. " );

                            for( String rs : sentenceArray ) {
                                sentence = rs.trim();
                                if( isRightSentence( sentence, engWord ) ) {
                                    splittedSentence = sentence
                                            .toLowerCase()
                                            .replaceAll( "[^a-zA-Z\\-']+", " " )
                                            .split( "\\s+" );

                                    if( !sentencePull.containsKey( sentence ) ) {
                                        sentencePull.put( sentence, calcAvrRankOfSentence( sortedMap, splittedSentence ) );
                                    }

                                    if( searchBreakerComplete( sentenceCounter, wordNum ) ) {
                                        rankingSentencePull = sortByValue( sentencePull );
                                        for( Map.Entry<String, Integer> entry : rankingSentencePull.entrySet() ) {
                                            bw.write( engWord.getKey() + "   ^^^   " + entry.getValue() + "   ###   " + entry.getKey() + ".\n" );
                                        }
                                        sentencePull.clear();
                                        rankingSentencePull.clear();
                                        bw.flush();
                                        break search;
                                    }
                                    sentenceCounter++;
                                }
                            }
                        }
                        searchedLog( sentenceCounter );
                    }
                }
            }
        } catch( Exception ignored ) {
            //
        }
    }


    private static Map<String, Integer> readFileToMap() throws FileNotFoundException {
        Map<String, Integer> map = new HashMap<>();
        FileInputStream inputStream;
        Scanner sc;
        inputStream = new FileInputStream( "src\\com\\company\\resources\\sortedWordsWithRank" );
        sc = new Scanner( inputStream, "UTF-8" );
        int counter = 0;
        while( sc.hasNextLine() ) {
            String line = sc.nextLine();
            String[] a = line.split( ":" );
            map.put( a[ 0 ].trim(), Integer.parseInt( a[ 1 ].trim() ) );
            counter++;
        }
        System.out.println( " Слова считаны. Всего " + counter );
        return map;
    }

    private static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue( Map<K, V> map ) {
        List<Map.Entry<K, V>> list = new LinkedList<>( map.entrySet() );
        list.sort( Comparator.comparing( o -> ( o.getValue() ) ) );

        Map<K, V> result = new LinkedHashMap<>();
        for( Map.Entry<K, V> entry : list ) {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }

    private static boolean searchBreakerIfFew( int sentenceCounter, int i ) {
        if( i % 100 == 0 ) {
            System.out.println( AnsiColor.YELLOW + " Считали уже " + i + " файлов wiki: " + sentenceCounter + " предложений найдено" + AnsiColor.RESET );
            if( i == 300 && sentenceCounter == 0 ) {
                System.out.println( searchBreked );
                return true;
            }
            if( i == 500 && sentenceCounter == 1 ) {
                System.out.println( searchBreked );
                return true;
            }
            if( i == 700 && sentenceCounter == 2 ) {
                System.out.println( searchBreked );
                return true;
            }
            if( i == 1000 && sentenceCounter <= 3 ) {
                System.out.println( searchBreked );
                return true;
            }
            if( i > 2000 && sentenceCounter <= 5 ) {
                System.out.println( searchBreked );
                return true;
            }
        }
        return false;
    }


    private static BufferedWriter createNewFile( String fileForWrite ) throws IOException {
        BufferedWriter bw;
        File f = new File( fileForWrite );
        f.createNewFile();
        f.setExecutable( true );
        f.setWritable( true );
        f.setReadable( true );
        bw = new BufferedWriter( new FileWriter( fileForWrite ) );
        return bw;
    }

    private static int calcAvrRankOfSentence( Map<String, Integer> sortedMap, String[] splittedSentence ) {
        int avrRank;
        int sumRank = 0;
        for( String ss : splittedSentence ) {
            sumRank += sortedMap.getOrDefault( ss.trim(), 100000 );
        }
        avrRank = sumRank / ( splittedSentence.length * 100 );
        return avrRank;
    }


    private static boolean searchBreakerComplete( int sentenceCounter, int wordNum ) {
        if( wordNum < 10001 ) {
            if( sentenceCounter == 50 ) {
                searchedLog( sentenceCounter );
                return true;
            }
        }
        if( wordNum > 10000 && wordNum < 50000 ) {
            if( sentenceCounter == 5 ) {
                searchedLog( sentenceCounter );
                return true;
            }
        }
        if( wordNum >= 50000 && wordNum < 100000 ) {
            if( sentenceCounter == 3 ) {
                searchedLog( sentenceCounter );
                return true;
            }
        }
        if( wordNum >= 100000 ) {
            if( sentenceCounter == 2 ) {
                searchedLog( sentenceCounter );
                return true;
            }
        }
        return false;
    }


    private static void searchedLog( int sentenceCounter ) {
        System.out.println( " Найдено всего " + sentenceCounter + " предложений " );
    }

    private static boolean isRightSentence( String sentence, Map.Entry<String, Integer> engWord ) {
        //sentence = sentence.toLowerCase();
        return sentence.length() > 20 &&
               sentence.length() <= 128 &&
               Character.isUpperCase( sentence.charAt( 0 ) ) &&
               isAlphaWithPunctuation( sentence ) &&
               !sentence.contains( "http" ) &&
               !sentence.contains( "www" ) &&
               ( ( sentence.contains( " " + engWord.getKey() + " " ) ) ||
                 ( sentence.toLowerCase().indexOf( engWord.getKey() + " " ) == 0 ) ||
                 ( sentence.endsWith( " " + engWord.getKey() ) )
               );
    }



    private static String readLineByLine( String... filePath ) {
        StringBuilder contentBuilder = new StringBuilder();
        for( String f : filePath ) {
            try (Stream<String> stream = Files.lines( Paths.get( f ), StandardCharsets.UTF_8 )) {
                stream.forEach( s -> contentBuilder.append( s.trim() ).append( " " ) );
            } catch( Exception e ) {
                //
            }
        }
        return contentBuilder.toString();
    }

    private static boolean isAlphaWithPunctuation( String name ) {
        return name.matches( "[A-Za-z .,!\"'/$]*" );
    }


    // Наладить только этот метод -
    private static String iterateByWiki( Integer i ) {
        if( i >= 1 && i < 10 ) {
            return readLineByLine( "C:\\wiki\\20140615-wiki-en_00000" + i + ".txt\\20140615-wiki-en_00000" + i +
                                   ".txt" );
        }
        if( i >= 10 && i < 100 ) {
            return readLineByLine( "C:\\wiki\\20140615-wiki-en_0000" + i + ".txt\\20140615-wiki-en_0000" + i + "" +
                                   ".txt" );
        }
        if( i >= 100 && i < 1000 ) {
            return readLineByLine( "C:\\wiki\\20140615-wiki-en_000" + i + ".txt\\20140615-wiki-en_000" + i + "" +
                                   ".txt" );
        }
        if( i >= 1000 && i < 4633 ) {
            return readLineByLine( "C:\\wiki\\20140615-wiki-en_00" + i + ".txt\\20140615-wiki-en_00" + i + ".txt" );
        } else {
            return "";
        }
    }
}
