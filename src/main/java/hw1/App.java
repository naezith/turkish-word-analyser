package hw1;
import zemberek.morphology.ambiguity.Z3MarkovModelDisambiguator;
import zemberek.morphology.analysis.SentenceAnalysis;
import zemberek.morphology.analysis.WordAnalysis;
import zemberek.morphology.analysis.tr.TurkishMorphology;
import zemberek.morphology.analysis.tr.TurkishSentenceAnalyzer;

import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class App {
    static TurkishSentenceAnalyzer sentenceAnalyzer;
    
    private static void writeParseResult(SentenceAnalysis sentenceAnalysis) {
        for (SentenceAnalysis.Entry entry : sentenceAnalysis) {
            if(!entry.parses.isEmpty()) {
	            WordAnalysis analysis = entry.parses.get(0);
                System.out.println("\n " + entry.input + "->" + analysis.getLastIg().pos.shortForm);
            }
        }
    }
    
    static void analyze(Map<String, Integer> stats, String sentence) {
        SentenceAnalysis result = sentenceAnalyzer.analyze(sentence);
        
        for (SentenceAnalysis.Entry entry : result) {
            if(!entry.parses.isEmpty()) {
	            WordAnalysis analysis = entry.parses.get(0);
	            
	            String type = analysis.getLastIg().pos.shortForm;
	            stats.put(type, stats.containsKey(type) ? stats.get(type) + 1 : 1);
            }
        }
    }
    
    static void printStats(String title, Map<String, Integer> stats) throws IOException {
    	System.out.println("\n- "+ title);
        for (Entry<String, Integer> entry : stats.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }
    }
    
    
    // Bar Chart
    static void fillDataset(String name, DefaultCategoryDataset dataset, Map<String, Integer> stats) throws IOException {
        for (Entry<String, Integer> entry : stats.entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());

            dataset.addValue(entry.getValue() , name , entry.getKey());            
        }
    }
    
    
    static void saveChart(String name, JFreeChart chart) throws IOException {
        File chartFile = new File(name + ".jpeg");                            
        ChartUtilities.saveChartAsJPEG(chartFile, chart, 842, 595);
    }

    static void saveStatsToBarChart(String name, Map<String, Integer> stats) throws IOException {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset(); 
        fillDataset(name, dataset, stats);
        saveChart(name, ChartFactory.createBarChart3D(name, "Form", "Count", dataset, PlotOrientation.VERTICAL, true, true, false));
    }
    
    // Pie Chart
    static void fillDataset(String name, DefaultPieDataset dataset, Map<String, Integer> stats) throws IOException {
    	int total = 0;
        for (Entry<String, Integer> entry : stats.entrySet()) total += entry.getValue();     
        
        for (Entry<String, Integer> entry : stats.entrySet()) {
        	double percent = 100.0 * (double)entry.getValue() / total;
            dataset.setValue(entry.getKey() + " (" + String.format( "%.1f", percent ) + ")", percent); 
        }
    }
    
    static void saveStatsToPieChart(String name, Map<String, Integer> stats) throws IOException {
        DefaultPieDataset dataset = new DefaultPieDataset();
        fillDataset(name, dataset, stats);
        saveChart(name, ChartFactory.createPieChart3D(name, dataset, true, true, true));
    }

    public static void main(String[] args) throws IOException {
        TurkishMorphology morphology = TurkishMorphology.createWithDefaults();
        Z3MarkovModelDisambiguator disambiguator = new Z3MarkovModelDisambiguator();
        sentenceAnalyzer = new TurkishSentenceAnalyzer(
                morphology,
                disambiguator
        );

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey("xxxxxxxxx");
        cb.setOAuthConsumerSecret("yyyyyyy");
        cb.setOAuthAccessToken("zzzzzzzzzz");
        cb.setOAuthAccessTokenSecret("wwwwwwwww");

        Twitter twitter = new TwitterFactory(cb.build()).getInstance();

        List<Status> statuses = new ArrayList<Status>();
        Paging page = new Paging(1, 50);
        String twitter_user = "ProfDemirtas";
        try {
			statuses.addAll(twitter.getUserTimeline(twitter_user, page));
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        // Analyze tweets
        Map<String, Integer> tweet_stats = new HashMap<String, Integer>();
        for(int i = 0; i < statuses.size(); ++i) {
        	String tweet = statuses.get(i).getText();
        	//System.out.println(tweet);
        	analyze(tweet_stats, tweet);
        }
        printStats("Tweets" + " (" + statuses.size() + " from " + twitter_user +")", tweet_stats);
        
        // Analyze article
        Map<String, Integer> article_stats = new HashMap<String, Integer>();
    	analyze(article_stats, article);
        printStats("Article (Sosyal Yardım Kuruluşlarında Yalın Uygulamalar)", article_stats);
        
        // Analyze news
        Map<String, Integer> news_stats = new HashMap<String, Integer>();
    	analyze(news_stats, news);
        printStats("News (Hürriyet.com.tr > Bitcoin nedir? Bitcoin Madenciliği nasıl yapılır?)", article_stats);
        
        
        // Charts
        saveStatsToBarChart("Tweets", tweet_stats);
        saveStatsToBarChart("Article", article_stats);
        saveStatsToBarChart("News", news_stats);
        
        saveStatsToPieChart("Tweets Pie", tweet_stats);
        saveStatsToPieChart("Article Pie", article_stats);
        saveStatsToPieChart("News Pie", news_stats);
    }
    

    
    static String article = "Toyota Kuzey Amerika Örneği " + 
    		"Toyota bünyesinde kurulu “Toyota Üretim Sistemi Destek Merkezi”nin (Toyota Production Support Center – TSSC)  Sosyal Amaçlı Destek Faaliyetleri İnsanları derinden Etkiliyor, ilham veriyor. " +  		 
    		"Yalın Enstitü kurulma aşamasına girdiği 2001 yılından günümüze her yıl Amerika Birleşik Devletleri’nde kurulu Lean Enterprise Institute (LEI)’nin organize ettiği yıllık Yalın Zirve’ye katılmayı adet edindik. Her yıl iki gün süre bu toplantılar sırasında çok ilginç konular, kişiler ve yalın uygulama örnekleri ile karşılaşıyoruz. Son iki zirvede karşılaştığım Toyota Sosyal Destek Merkezi’nin aktif olarak katıldığı iki çok önemli sosyal sorumluluk aktivitesinin sunumlarını büyük bir ilgi ile izledim. Bunlardan biri New York’ta açılan ‘’Yoksullara Parasız Yemek Servis Merkezi’’nde Toyota Sosyal Destek Merkezi’nin aktif katılımı ile  gerçekleştirilen Yalın Düşünce uyarlamaları ile ilgili proje idi. " + 
    		"Geçtiğimiz Yalın Zirve’ de yemek merkezinin müdürü ve merkezin çalışanları büyük bir gurur ve neşe ile sunumlarını yaptılar, yapılan çalışmalar ile ilgili videoları izlettirdiler. Toplantıya katılan bizler o kadar etkilendik ki sunum sonunda hepimiz ayağa kalkıp bu gururlu ve bir o kadar hayat dolu insanlar candan alkışladık. TSSC ve faaliyetleri ilgimizi o kadar çekmişti ki,  TSSC adına sunuma katılan Jamie Bonini ile tanıştım ve ayak üstü de olsa konuştum. Bonini yıllardır Toyota’da yönetici olarak çalışmış; kendisine bizim de sosyal sorumluluk ile ilgili bir projemiz olursa Türkiye’ye gelebilme imkanının olup olmadığını sormuştum. O tarihlerde Kızılay ile kapsamlı bir çalışma yapmak için çaba gösteriyorduk. " + 
    		"TSSC Merkezi ve yaptığı Sosyal Amaçlı Yalın Dönüşüm Projeleri ile o kadar ilgilenmiştim ki daha çok bilinmesi için Emir Tunç Demirel ile birlikte daha fazla internet araştırması yaptık ve bu satırları kaleme aldım. " + 
    		"Yalın Enstitü Amerika’nın bu yılki Yalın Zirvesi bu sefer Kaliforniya’da yapıldı ve bu zirveye TSSC diğer bir Sosyal Yardım Projesi ile damgasını vurdu. Bu seferki sunum yoksulların evleri restore eden St. Bernard Project isimli hayır kuruluşu ile yapılan ‘’Yalın Proje’’yi konu alıyordu. Zirvede St.Bernard’ı kuran bayan ve eşi ile proje yöneticileri ve Jamie Bonini katıldılar. Ve yine ayakta alkışlandılar. Projede evvelce bir evi restorasyonu 116 günde tamamlanırken TSSC desteği ile 60 güne indirilmiş, ayda 8.6 ev restore edilirken bu sayı ayda 12.8 çıkarılmıştı. " + 
    		"Katrina Kasırgası sonrası New Orleans ve civarındaki sel felaketi sırasında ortaya çıkan güç koşullar altında kalan, evlerini yitiren aç açık kalan insanlara yaptıkları inanılması güç hizmetin sunumunu ve videosunu aşağıdaki linkten izleyebilirsiniz… " + 	 
    		"New York Yoksulları Doyurma Merkezi ile ilgili olarak TSSC’nin yaptığı çalışmaya Buket Kanber arkadaşım internette rastlamış, kendisinin çok ilgisini çekmiş ve New York Times’ın haberini tercüme ederek Yalın Enstitü sitesinde yayınlamak için web sorumlumuz Yasin Demirkaya’ya temas etmiş, neticede bu haber web sitemizde yer aldı. " + 
    		"TSSC Kan Dağıtım Merkezi, America Cincinnati Çocuk Hastanesi ve Sağlık Sigortası olmayan Vatandaşlara ücretsiz Sağlık Hizmet Veren Klinik ve benzeri yıllık  50 sosyal projeye 15 kişilik Yalın Danışman ile destek verdiklerini not etmek istiyoruz. " + 
    		"Bu bilgi notuna ilave olarak, TSSC internet sitesine ait linki paylaşıyoruz. Toyota’nın Sosyal amaçlı çalışmalarını buradan daha kapsamlı olarak inceleyebilirsiniz. " + 
    		"Bir küçük gazete haberinden yola çıkarak geldiğimiz noktada; " + 
    		"    Yalın Düşünce’yi uygulamalar yaparak ve iş sonuçları alarak samimi olarak benimsemiş, içselleşmiş yalın düşünce uygulayıcıların yaparak ve yaşayarak öğrendikleri düşünce, kültür ve tatbikatı her alana devşirmesi ve üretim alanında elde ettikleri sonuçları almaktadırlar, Benzeri uyarlamaları bizlerde Türkiye’de sıklıkla yaşıyoruz. May Tohum ile yıllar önce yaptığımız Yalın Çalışmalar ve tarlada köylüler ile birlikte aldığımız sonuçları bugünkü gibi hatırlıyorum. " + 
    		"    Sosyal Sorumluluk konusunda Toyota’nın örnek faaliyetlerinin ne kadar ileri gittiğini anlamak ve anlatmanın gerekli olduğuna inanıyorum. Günümüzde sosyal sorumluluk güncel bir konu haline geldi. Kurulu şirketlerimiz, çalışanları ile birlikte sosyal sorumluluk adına güzel işler yapıyorlar. Bu çalışmaların ve faaliyetlerin sayısını arttırmak gerekiyor. Geçen haftalarda ziyaret ettiğim ve hakkında bir yazı kaleme aldığım TÜPRAG Efem Çukuru Altın Madeni’nde karşılaştığım sosyal sorumluluk anlayışı çok çarpıcı bir örnek teşkil etmektedir. Hep menfi haberlere gündeme gelen madencilik ve bilhassa altın madenciliğinde sosyal sorumluluğun en düzeyini gerçekleştirmenin mümkün olduğunu görmek ve şahit olmak beni keyiflendirdiği gibi şahit olduklarımı sizlerle paylaşmayı kendimde bir görev bildim. " + 		 
    		"Emir Tunç Demirel ile birlikte araştırmamız sonunda sosyal sorumluluk alanında Toyota kaynaklı önemli bilgilere rastladık. Bu bilgileri sizlerle paylaşmak arzusundayız. " + 
    		"Toyota’nın İŞ Kültürü’nü büyük ölçüde etkilemiş olan Dede Sakichi Toyoda’dan kaynaklanan ve Toyota Global Web sitesinden aktardığımız Toyota’nın Yol Gösterici İlkelerini ilgisi dolayısıyla sizlerle paylaşıyoruz: " + 
    		"TOYOTA’nın Yol Gösterici İlkeleri " + 
    		"    İyi bir dünya vatandaşı olmak için her ulusun yasalarının diline ve ruhuna saygı göster ve iş faaliyetlerinde açık ve adil ol. " + 
    		"    Her ulusun kültürüne ve geleneklerine saygı göster ve kurumsal faaliyetler yoluyla toplumların ekonomik ve sosyal gelişimine katkı sağla. " + 
    		"    Tüm şirket faaliyetlerimizde temiz ve güvenli ürünler sunmayı ve her yerde yaşam kalitesini artırmayı öncelik haline getir. " + 
    		"    İleri teknolojiler geliştir ve müşterilerimizin ihtiyaçlarını karşılayacak mükemmel ürünler ve hizmetler sun. " + 
    		"    Hem bireysel yaratıcılığı hem de ekip çalışmasının değerini yükseltirken çalışanlar ve yönetim arasında karşılıklı güven ve saygıyı artıran bir kurumsal kültür geliştir. " + 
    		"    Yenilikçi yönetim yoluyla global toplumla uyum içinde büyümeyi hedefle. " + 
    		"    İstikrarlı ve uzun vadeli büyüme ve karşılıklı fayda için araştırma ve üretimde iş ortaklarıyla birlikte çalışırken yeni iş ortaklıklarına da açık ol. ";
    
    
    static String news = "Bitcoin nedir? Bitcoin ile nasıl para kazanılır? Bitcoin kaç TL? Bitcoin sadece internet üzerinden mi kullanılabiliyor? Bitcoin nasıl kazanılır? Bitcoin nasıl harcanır? Bitcoin'e yatırım yapmak mantıklı mı? Uzmanlar Bitcoin için ne diyor? Bitcoin'in değeri önümüzdeki 6 ayda ne olacak? Yükselecek mi yoksa çakılacak mı? Bitcoin nedir sorusuyla başlayalım: Bitcoin 008 yılında Satoshi Nakamoto tarafından deneysel olarak başlatılmış, herhangi bir merkez bankası, resmi kuruluş, vs. ile ilişiği olmayan, ancak ülkelerin para birimleriyle alınıp satılabilen, herhangi bir üçüncü parti hizmetine aracı kurumuna gerek kalmadan transferi yapılabilen bir tür dijital para birimidir. Küresel piyasalarda, Dolar ve Euro’ya alternatif olarak lanse edilen Bitcoin’in sembolü ฿, kısaltma ise BTC‘dir. " + 
    		"Soru: Bitcoin kağıt paranın yerine geçecek mi? " + 
    		"Bitcoin sanal para birimi; ancak kağıt paranın yerine geçip geçmeyeceği bir süredir tartışılıyor. Ciddiye alınması gereken bir para birimi olduğu çok açık; ancak kağıt paranın yerine geçeceğini düşünmek biraz gerçeklerden uzak.  " + 
    		"Soru: Bitcoin nasıl üretilir? Nasıl kazanılır? " + 
    		"Bitcoin üretim aşaması için mining yani madencilik tabiri kullanılıyor.  " + 
    		"Aslında Bitcoin madenciliğinin nasıl yapıldığından çok; neden çok önemli olduğu kendi başına uzunca bir yazıyı hak ediyor. Çünkü Bitcoin’i bu kadar önemli bir teknoloji ve büyük bir buluş yapan şeyin kilit noktalarından bir tanesi Proof-of-Work olarak bilinen konsept. Bu yazıda ise bu konuya kabaca değinip işin teknik kısmına çok da kafa yormak istemeyen okurlarımızın akıl sağlığını koruyacağım, en azından şimdilik. " + 
    		"Bitcoin nedir Bitcoin Madenciliği nasıl yapılır " + 
    		"Zor bulunmasının ve zorlaşmasının sebebi ise şans eseri değil. Bunun sebebi Bitcoin protokolü tasarlanırken, Satoshi Nakamoto tarafından ayarlanmış bir güvenlik önlemi olması. Bu güvenlik önlemi, (yani bulunmasının zor olması) herkes kafasına göre “Bitcoin basma” işlemini gerçekleştiremesin (ve/veya Blockchain’i manipüle edemesin…) diye var. " + 
    		"Aslında madencilik denilen şeyi, bilgisayarınıza oynattığınız bir şans oyunu ya da yukarıda yazdığım gibi, rakamlardan oluşan bir saman yığınında, yine rakamlardan oluşan bir iğne aratmak gibi düşünün. " + 
    		"Burada her bir yaptığınız arama hamlesini de, işlemci gücünüzle doğru orantılı olduğunu varsayın. Bu durumda bu işe ne kadar çok işlemci gücü yatırırsanız, saman yığınında bir iğne bulma olasılığınız o kadar artacaktır. " + 
    		"Mining için kullanılan bir başka benzetme ise bilgisayarınıza kura çektirmek. Teknik olarak daha doğru olsa da, akla “şanslı olanın kazanacağı” bir sistem getirdiği için ben bunu tercih etmedim. Çok fazla şansa dayalı olmamasının sebebi ise, kura çekilen havuzun çok büyük olması ki, sadece ve sadece kura çekimi işine yüklü miktarda emek (işlemci gücü) yatıranların bu sözde “çekilişi” sürekli kazanacağı gerçeği. " + 
    		"Şu anki durumda (BTC protokolünün günümüz için belirlediği zorluk ve ödül seviyesinde), bu iğneyi bulan (yani “çekilişi kazanan”) madenci 25 BTC ödül kazanıyor. Bu rakam Bitcoin’in ilk zamanlarında 50BTC idi ve git gide azalacak [bir sonraki ödül adımının 2017 yılında başlaması bekleniyor ve o zaman ödül 12,5BTC’ye düşmüş olacak]. " + 
    		"Bitcoin nedir Bitcoin Madenciliği nasıl yapılır " + 
    		"Buraya kadar sıkılmadan geldiyseniz, en azından Bitcoin madenciliği/kazıcılığı hakkında bir miktar fikriniz oldu demektir. Ama özet geçmek gerekirse; madencilik zor ve çok miktarda işlemci gücü gerektiriyor, zorluğu giderek artıyor, bu olurken de ödül miktarı azalıyor. " + 
    		"Soru: Bitcoin nasıl kullanılıyor? " + 
    		"Bitcoin için sanal cüzdan gerekiyor, oluşturmak da gayet basit. Bu sanal cüzdanınızı cep telefonunuzda, bilgisayarınızda oluşturabileceğiniz gibi internet üzerinde bulunan web servislerinden de yararlanabilirsiniz. Kişi başına cüzdan sınırı bulunmuyor. Dilediğiniz kadar cüzdan oluşturabilirsiniz. Üstelik oluşturacağınız cüzdan için özel bilgilerinizi vermenize de gerek yoktur. Oluşturacağınız bu sanal cüzdan ile para alabilir, para gönderebilir hatta alış veriş bile yapabilirsiniz. " + 
    		"Bitcoin sisteminde yapılan ödemelerin doğrulanması için açık anahtarlı şifreleme (asimetrik şifreleme), noktadan-noktaya ağ bağlantısı ve proof-of-work gibi teknolojiler kullanılıyor. Bitcoinler ödemeyi yapan adresten alıcı adrese şifrelenmiş olarak imzalanarak gönderilir. Her işlemin ağa duyurulumu yapılır ve blok zincirinde yerini alır. Böylece eklenen bitcoinler birden fazla kere kullanılamaz. Bitcoin bu teknolojileri kullanarak, herkesin kullanabileceği hızlı ve son derece güvenilir bir ödeme ağı sağlamaktadır. " + 
    		"Soru: Bitcoin ne kadar güvenilir? " + 
    		"Bitcoin belli başlı protokollere tabidir. Bu protokoller çerçevesince yaptığınız her işlem şifrelenir. Aynı zamanda Bitcoin yapı itibariyle sağlam bir işlem kayıt hafızasına sahiptir. Cüzdanınızın şifresini çaldırmanız ya da bilgisayarınızın hacklenmesi gibi kullanıcı hatası ya da dikkatsizliği sonucu oluşan durumlar dışında sistemin güvenlik sorunu bulunmamaktadır. Bu durum cüzdanınızı ya da kredi kartınızı kaybetmekten ya da çaldırmaktan farksızdır. Bitcoin kriptosu gereği aynı paranın iki kez harcanması mümkün değildir. Paranın size ait olduğunu ve daha öncesinde başka birisine gönderilmediği, işlem öncesinde sistem tarafından teyit edilir. Bu nedenle kontrolsüz bir şekilde, hileli yollarla Bitcoin yaratıp satmak mümkün değildir. " + 
    		"BITCOIN KULLANMANIN AVANTAJLARI " + 
    		"Enflasyon riski düşük: Enflasyona sebebiyet veren etkilerden biri de tedavüldeki reel para arzlarındaki artıştır. Tedavüldeki para arzının artması doğru orantılı olarak enflasyonu da arttırır. Ancak bu sistem Bitcoin için geçerli değildir. Çünkü Bitcoin sistemi, sonu olan bir sistemdir. Teknik tasarımı itibariyle maksimum 21 milyon adet Bitcoin üretilebilir. Dolayısıyla Bitcoin’in enflasyon riski çok düşüktür. " + 
    		"Bitcoin nedir Bitcoin Madenciliği nasıl yapılır " + 
    		"Çökme riski düşük: Reel para birimlerinin çöküşü, hükümetlere bağlı yaşanan hiperenflasyonlardan kaynaklanır. Bitcoin sistemi de herhangi bir hükümete bağlı olmadığı için, çökme riski oldukça düşüktür. " + 
    		"Güvenli, basit ve ucuz: Kredi kartı veya PayPal gibi ödeme sistemlerinin kullanıldığı klasik online işlemlerde satıcı tarafından bakılırsa; alıcının parasını geri talep etmesi durumunda 3. şahıs servislerini kullanmak gerekiyor. Bu aşamada güvenlik tehlikeye girebiyor ve olay karmaşık bir hale dönüşebiliyor. Bitcoin’de ise geri talep etme gibi bir sistem olmadığı için bu tarz bir güvenlik sorunu da haliyle yaşanmıyor. Olaya alıcı tarafından bakılırsa da; ödeme yapma ve hesaplar arası para transferi yapma gibi işlemler daha güvenli ve daha ucuz hale geliyor. Çünkü Bitcoin teknolojisi peer to peer (P2P) sistemiyle; yani hiç bir aracı olmadan direkt olarak eşler-arası çalışıyor. " + 
    		"Taşıması kolay: Milyarlarca dolar değerindeki bitcoinlerinizi küçük bir hafıza kartında bile taşıyabilirsiniz. Bunu nakit para ile yada başka bir sistem ile yapmak imkansız sanırım. " + 
    		"İzi sürülemez: Bitcoinlerin takip edilemiyor olmasının yada herhangi bir iz bırakmamasının avantajları olduğu kadar dezavantajları da var muhakkak. Örneğin Bitcoin sisteminizdeki mali kaynağınızın ne kadar olduğu yada hesabınız hakkındaki diğer bilgiler, hükümetler de dahil olmak üzere hiçkimse tarafından bilinemez ve takip edilemez. Dolayısıyla hükümetlerin mali kaynağınıza göz dikmesini (vergi) önlenmiş olursunuz. " + 
    		"BITCOIN KULLANMANIN DEZAVANTAJLARI " + 
    		"İzi sürülemez: Avantajlarından bahsettiğimiz Bitcoin’in bu özelliği pek tabi dezavantaja hatta tehlikeli durumlara da dönüşebilir. İzinin sürülemez olması, suçların rahat rahat işlenebilmesi anlamına geliyor. Uyuşturucu gibi yasal olmayan maddelerin satılması tarzı durumlar için oldukça uygun bir ortam hazırlıyor Bitcoin teknolojisi. Bazı hükümetlerin Bitcoin’i bir para birimi olarak kabul etmemelerinin en büyük sebebi ise budur. " + 
    		"Kaybetmesi kolay: Kredi kartınızı kaybettiğinizde yada banka hesabınız başka birisi tarafından ele geçirildiğinde, bankanızı arayarak durumu anında kurtarabilirsiniz. Hatta yetmezse polisi bile devreye sokup kaybınızı giderebilirsiniz. Ancak Bitcoin için aynı şeyler geçerli değil. Bitcoin sisteminde; kaybettiğiz bitcoinleri, yada ele geçirilen Bitcoin cüzdanınızı geri almak için kullanabileceğiniz bir mekanizma yok. Bunun önüne geçebilmek için izleyebileceğiniz en iyi yöntem, Bitcoinlerinizi internet bağlantısı olmayan harddisklerde saklamak olacaktır. " + 
    		"Alıp satmak zor: Taşıdığı risklerden dolayı Bitcoinlerinizi bir ürün satın alır gibi alamıyorsunuz. Bitcoin satın almak veya bitcoinleriniz satmak için birçok servis bulunuyor ancak bunu gerçekleştirmek o kadar da kolay değil. Her geçen gün geliştirilmesine rağmen (Bitcoin ATM’leri açılmaya başlandı), işler reel para birimlerinde olduğu kadar kolay ilerlemiyor. " + 
    		"Hala çok yeni: Bitcoin sisteminin 2009 yılında kurulmuş olması; yani hala yeni bir teknoloji olması, yeni Bitcoin rakiplerini de beraberinde getirebilir; hatta Bitcoin sisteminde büyük açıklıklar bulabilen yeni teknolojiler hala geliştirilebilir. " + 
    		"Harcama alanı darlığı: Ödeme sistemi olarak Bitcoin sisteminin de kullanıldığı çok fazla yer yok. Her ne kadar gelişim aşamasında olsa da, şu anda Bitcoin sahibi olmaktaki en büyük amaç; yatırım. " + 
    		"Değişken: Bitcoin’in değer grafiğine baktığımızda herkesin aklından Bitcoin yatırımı yapmak geçebilir, ancak Bitcoin değeri nasıl hızla yükseldiyse düşme riski de o kadar fazla. Dolayısıyla yapılacak olan yatırımların dengeli ve düşünerek yapılması gerekmektedir.";
}
