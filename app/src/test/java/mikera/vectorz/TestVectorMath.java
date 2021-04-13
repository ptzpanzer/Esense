package mikera.vectorz;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import mikera.indexz.Index;

public class TestVectorMath {

	@Test public void testBasicAddCopy() {
		assertEquals(Vector.of(3.0),Vector1.of(1.0).addCopy(Vector1.of(2.0)));
	}

	@Test public void testIndexedDotProduct() {
		Vector v1=Vector.of(0,1,2,3,4,5,6,7,8,9);
		Vector v2=Vector.of(1,2,3);
		Index ix=Index.of (2,7,4);

		assertEquals((1*2)+(2*7)+(3*4),v1.dotProduct(v2,ix),0.0);
	}

	@Test public void testSubVectorMultiply() {
		Vector v1=Vector.of(1,2,3,4,5);
		Vector v2=Vector.of(2,3,4,5,6);

		AVector a=v1.subVector(2, 2);
		AVector b=v2.subVector(3, 2);
		a.multiply(b);
		assertEquals(15.0,v1.get(2),0.0);
		assertEquals(24.0,v1.get(3),0.0);

		assertEquals(Vector.of(5,6),b);

		v1=Vector.of(1,2,3,4,5);
		v1.multiply(v2);
		assertEquals(Vector.of(2,6,12,20,30),v1);
		assertEquals(Vector.of(2,3,4,5,6),v2);
	}

	@Test public void testSubVectorMultiply2() {
		Vector v1=Vector.of(1,2,3);
		AVector v2=Vector.of(1,2,3,4,5).subVector(1, 3);

		v1.multiply(v2);
		assertEquals(Vector.of(2,6,12),v1);
		assertEquals(Vector.of(2,3,4),v2);
	}

	@Test public void testDotProduct() {
		assertEquals(10.0,new Vector3(1,2,3).dotProduct(new Vector3(3,2,1)),0.000001);
	}

	@Test public void testProjection() {
		Vector3 v=Vector3.of(1,2,3);
		v.projectToPlane(Vector3.of(1,0,0), 10);
		assertTrue(Vector3.of(10,2,3).epsilonEquals(v));
	}

	@Test public void testMagnitude() {
		assertEquals(14.0,new Vector3(1,-2,3).magnitudeSquared(),0.000001);
		assertEquals(5.0,new Vector2(3,4).magnitude(),0.000001);
	}


	public void doMultiplyTests(AVector v) {
		v=v.exactClone();
		if (!v.isFullyMutable()) return;
		double m=v.magnitude();
		v.multiply(2.0);
		assertEquals(m*2.0, v.magnitude(),0.0001);

		AVector vv=v.exactClone();
		vv.set(0.5);
		v.multiply(vv);

		assertEquals(m, v.magnitude(),0.0001);
	}

	public void doNormaliseTests(AVector v) {
		v=v.clone();
		v.normalise();
		if (v.magnitude()>0.0) {
			assertEquals(1.0,v.magnitude(),0.0001);
		}
	}

	public void doFillTests(AVector v) {
		v=v.clone();
		v.fill(13.0);
		int len=v.length();
		for (int i=0; i<len; i++) {
			assertEquals(13.0,v.get(i),0.0);
		}
	}

	public void doAdditionTests(AVector v) {
		v=v.clone();
		AVector ones=v.clone();
		ones.fill(1.0);

		AVector av=v.clone();
		av.add(ones);
		av.addMultiple(ones,1.5);

		int len=v.length();
		for (int i=0; i<len; i++) {
			assertEquals(v.get(i)+2.5,av.get(i),0.0001);
		}
	}

	public void doWeightedTests(AVector v) {
		v=v.clone();

		AVector a=v.clone();
		Vectorz.fillRandom(a);
		AVector b=a.clone();

		b.addWeighted(v,0.0);
		assertTrue(b.epsilonEquals(a));

		b.addWeighted(v,1.0);
		assertTrue(b.epsilonEquals(v));

	}

	public void doSubtractionTests(AVector v) {
		v=v.clone();
		AVector ones=v.clone();
		ones.fill(1.0);

		AVector av=v.clone();
		av.add(ones);
		av.sub(ones);
		assertEquals(v,av);

		av.addMultiple(ones,4);
		av.subMultiple(ones,1.5);

		int len=v.length();
		for (int i=0; i<len; i++) {
			assertEquals(v.get(i)+2.5,av.get(i),0.0001);
		}
	}

	private void doMagnitudeTests(AVector v) {
		assertEquals(v.magnitude(),Vectorz.create(v).magnitude(),0.000001);

	}
	public void doGenericMaths(AVector v) {
		doFillTests(v);
		doMultiplyTests(v);
		doAdditionTests(v);
		doWeightedTests(v);
		doSubtractionTests(v);
		doNormaliseTests(v);
		doMagnitudeTests(v);
	}



	@Test public void testGenericMaths() {
		doGenericMaths(new Vector3(1.0,2.0,3.0));
		doGenericMaths(Vectorz.create(1,2,3,4,5,6,7));
		doGenericMaths(Vectorz.join(new Vector2(1.0,2.0),Vectorz.create(1,2,3,4,5,6,7)));
		doGenericMaths(Vectorz.create(1,2,3,4,5,6,7).subVector(2,3));

		for (int dim=0; dim<10; dim++) {
			AVector v=Vectorz.newVector(dim);
			doGenericMaths(v);
		}

	}

	@Test public void test3DMath() {
		Vector3 v=Vector3.of(1,2,3);

		Vector3 v2=v.clone();
		v2.add(v);
		v2.multiply(0.5);

		assertTrue(v.epsilonEquals(v2));
	}

	@Test public void testAngle() {
		Vector3 v=Vector3.of(1,2,3);
		assertEquals(0.0, v.angle(v),0.0001);

		Vector3 v2=v.clone();
		v2.negate();
		assertEquals(Math.PI, v.angle(v2),0.0001);
	}

	@Test public void testNumericalstability() {
		Vector v1 = Vector.of(-0.014672474935650826,-0.060921479016542435,0.10087049007415771,0.0153540950268507,0.03956206887960434,-0.07762990891933441,0.004401583690196276,-0.00851432979106903,-0.016627691686153412,-0.06950952857732773,0.01648535020649433,-0.021116597577929497,-0.06405960768461227,0.061415478587150574,-0.01646127924323082,0.03241392597556114,-0.0034250104799866676,0.08757651597261429,0.011445310898125172,-0.034614745527505875,0.03787026181817055,0.01969301886856556,-0.03634876385331154,-0.07617232948541641,-0.04989750683307648,0.018605932593345642,-0.0054916515946388245,0.029090989381074905,-0.01033382024616003,-0.0642751082777977,0.008981575258076191,0.03339681774377823,0.07036326825618744,-7.910212152637541E-4,0.025490863248705864,-0.03419310599565506,-0.02896084450185299,-0.04614390805363655,0.025531871244311333,-0.04316532984375954,-0.06870091706514359,-0.05159005895256996,-0.07589735835790634,-0.049602098762989044,0.02724762074649334,0.044040169566869736,0.030480124056339264,0.03235642611980438,0.03056095726788044,0.024091778323054314,-6.371267954818904E-4,0.02347361110150814,-0.002501036738976836,0.027920207008719444,-0.07511789351701736,0.012127351947128773,0.020146096125245094,0.006251493003219366,0.038035549223423004,-0.006672256626188755,0.031211068853735924,0.014829334802925587,-0.05150032788515091,-0.020497363060712814,0.04383742809295654,-0.03431278094649315,-0.03843933716416359,0.0150043535977602,-0.04187929630279541,0.059227555990219116,0.004107493907213211,-0.03497280925512314,-0.029103528708219528,0.003289624350145459,-0.08881937712430954,-0.008961683139204979,0.004717086907476187,0.02223982661962509,0.055549900978803635,-0.0161878764629364,0.024245597422122955,-0.01717694289982319,0.022717108950018883,0.06829257309436798,0.0635230541229248,0.004510267172008753,-0.06420350074768066,0.07498708367347717,0.07041779160499573,-0.06608670949935913,-0.043637268245220184,0.04252871870994568,0.013332311064004898,-0.008025133982300758,-0.030660146847367287,0.04573838785290718,-0.05960845202207565,0.020646514371037483,-0.05503839626908302,0.04121457040309906,0.08484749495983124,0.009258708916604519,0.01025861781090498,0.018627909943461418,-0.018932437524199486,0.008254818618297577,-0.02264932170510292,0.033970367163419724,0.06495217978954315,0.01382463425397873,0.0036916856188327074,-0.04240090772509575,-0.0912410244345665,-0.02600041963160038,0.02177176997065544,-0.014709528535604477,-0.02155478112399578,-0.04020718112587929,0.022628022357821465,-0.0989045575261116,0.009882892481982708,0.018138116225600243,0.03373369202017784,-0.06884878128767014,0.012620066292583942,-0.025955509394407272,-0.05680656433105469,0.0826617032289505,-0.05149027332663536,0.006291543133556843,0.025192342698574066,0.018419062718749046,-0.015112343244254589,0.023220116272568703,0.052635300904512405,0.005093525629490614,0.0545252189040184,0.039942674338817596,-0.019692622125148773,-0.03734058514237404,0.0588541179895401,-0.04563178867101669,0.04373469948768616,-0.04543791711330414,-0.05744180083274841,0.01720317266881466,-0.0854082927107811,-0.03623298183083534,-0.019212469458580017,0.05736434832215309,0.056226328015327454,0.004107156302779913,-0.027757612988352776,-0.03962142392992973,-0.04337155818939209,0.0029392745345830917,0.0712227076292038,-0.0540006048977375,0.01963852345943451,-0.05356534570455551,-0.052014924585819244,0.08905542641878128,0.017454756423830986,-0.011507829651236534,0.03509567677974701,0.0125507777556777,0.03138379380106926,0.024216271936893463,-0.050657689571380615,-0.025456245988607407,-0.01472635380923748,-0.08422835916280746,-0.09253020584583282,0.06076696142554283,-0.05572471395134926,0.013225755654275417,0.04623087868094444,0.01793598011136055,0.07384555786848068,0.008312879130244255,-0.0380166620016098,-0.046949807554483414,-0.05824628472328186,0.004187663085758686,-0.02620762400329113,-0.017816975712776184,0.03054753504693508,0.009359095245599747,0.011434297077357769,0.04951866343617439,-0.05787675082683563,0.03459971770644188,-0.07160191982984543,0.0027130518574267626,0.04751984775066376,-0.010564509779214859,0.08280697464942932,0.0769881084561348,-0.0314154289662838,0.04879987612366676,-0.013131959363818169,0.02883918583393097,-0.03760639578104019,-0.028443802148103714,-0.006969589274376631,-0.0415022112429142,0.018564512953162193,0.0040444862097501755,0.020479293540120125,-0.014797110110521317,0.048078637570142746,-0.07342076301574707,0.040382057428359985,0.06267718225717545,-0.03097708337008953,0.08996868133544922,0.026140186935663223,0.006135874427855015,0.10458917915821075,-0.0499354749917984,0.05887168273329735,0.0037474457640200853,-0.07183420658111572,-0.09926125407218933,-0.01864713430404663,-0.0078071593306958675,-0.009292719885706902,0.03932597488164902,0.06689969450235367,0.05718696862459183,0.04907668009400368,0.013830272480845451,-0.02062700130045414,0.06239774078130722,0.05830024927854538,-0.09595858305692673,0.027675090357661247,-0.033106349408626556,0.026968302205204964,0.054775115102529526,-0.03282015025615692,0.004955218639224768,-0.02857114002108574,-0.0373460091650486,0.06969446688890457,-0.029710909351706505,-0.03563770279288292,0.04336013272404671,-0.041999753564596176,-0.11235130578279495,0.027443813160061836,-0.01919204369187355,-5.070360493846238E-4,0.0020254417322576046,-0.043836191296577454,0.048074278980493546,-0.07363380491733551,0.07570996135473251,-0.01560026966035366,-0.062470607459545135,0.006533526815474033,0.003846443723887205,-0.058247700333595276,-0.03624064475297928,0.055345263332128525,0.036538902670145035,0.08495946228504181,-0.0964236706495285,0.006666433997452259,0.07158039510250092,0.0941976010799408,-0.02965710312128067,-0.04879981651902199,0.024249060079455376,0.04123895987868309,0.03856048732995987,-0.018006760627031326,-0.07077029347419739,0.034668929874897,-0.001403667381964624,0.08379256725311279,0.0028605484403669834,-0.013252174481749535,0.01622060313820839,-0.041202399879693985,-0.01771884225308895,-0.05962781980633736,0.05389392748475075,0.035289160907268524,-0.03918454051017761,0.011331198737025261,0.04514545947313309,-0.04957684874534607,-0.07102601230144501,0.035673972219228745,-0.008051143027842045,-0.06581827998161316,0.053875528275966644,-0.027136625722050667,0.011802584864199162,-0.012990973889827728,-0.07318586111068726,0.006141304038465023,-0.004976529162377119,0.017841869965195656,0.019139785319566727,-0.023918459191918373,0.03851857781410217,-0.05198444053530693,0.023321101441979408,0.008677342906594276,-0.057300787419080734,-0.04280127212405205,-0.04708677530288696,0.030500398948788643,-0.054328154772520065,-0.042221568524837494,0.046578165143728256,-0.028197424486279488,-0.05499295890331268,-0.010021836496889591,0.017890794202685356,0.03439932316541672,-0.026512403041124344,-0.0027062806766480207,0.02413947321474552,-0.018246743828058243,-0.010374336503446102,0.031514525413513184,0.08428751677274704,-0.017485179007053375,-0.04065853729844093,-0.028235774487257004,0.08206970989704132,0.012247757986187935,0.02642357163131237,0.06565714627504349,-0.06611126661300659,0.014827042818069458,0.09523377567529678,-0.013245407491922379,-0.024334093555808067,0.014618813060224056,-0.043163593858480453,-0.005913678091019392,-0.031274858862161636,-0.013277742080390453,0.047811511904001236,0.04316306859254837,0.06522604823112488,0.08264916390180588,0.013731591403484344,-0.04609978199005127,-0.056467678397893906,0.009641322307288647,0.014174544252455235,0.0187104232609272,0.0374431349337101,0.035957034677267075,0.021461954340338707,0.004401089157909155,-0.03889656439423561,-0.04913324490189552,-0.016310976818203926,-0.02966526336967945,0.01092937309294939,-0.05317120999097824,0.02729460783302784,-0.06934302300214767,-0.03283608332276344,0.012706915847957134,-0.07476099580526352,-0.07970266789197922,0.01850113272666931,0.035026825964450836,0.011481347493827343,-0.06983351707458496,0.06622762978076935,-0.004287383519113064,0.02485404536128044,0.013271123170852661,-0.009022021666169167,-0.05084630846977234,0.033457107841968536,-0.03561608865857124,0.003862060373649001,0.023318462073802948,-0.013211940415203571,-0.035802725702524185,-0.031690169125795364,0.11517245322465897,0.027899883687496185,0.09763495624065399,-0.01473498810082674,0.014655828475952148,-0.03888535499572754,0.05403246730566025,-0.01882738061249256,0.042323723435401917,-0.07802210748195648,-0.03363383561372757,-0.013654928654432297,0.0669025406241417,-0.006674893666058779,0.058964330703020096,0.07374133169651031,9.022817830555141E-4,-0.022628292441368103,0.016833335161209106,-0.029718732461333275,0.09526200592517853,0.017205052077770233,-0.03896680846810341,-0.042477115988731384,-0.016174279153347015,-0.007498868275433779,-0.010973247699439526,0.016591034829616547,0.007044659461826086,-0.026406990364193916,-0.026990795508027077,-0.01155818346887827,0.07962888479232788,0.02354908175766468,0.0635179951786995,0.049256835132837296,0.07857359200716019,-0.056579213589429855,-0.05932610109448433,0.006638250779360533,-0.0982515886425972,0.0035712309181690216,-0.028197873383760452,0.04056137055158615,0.0012351424666121602,0.02043057791888714,0.04588925838470459,-0.015251804143190384,0.04060744121670723,-0.025710562244057655,-0.03255010023713112,-0.07972458750009537,-0.021347513422369957,-0.047134578227996826,0.051518622785806656,0.14302733540534973,0.009764489717781544,-0.02981240302324295,0.020388782024383545,-0.006143294274806976,0.037423934787511826,-0.027064388617873192,0.01941872574388981,-0.014085566624999046,-0.004230785649269819,0.03489984944462776,0.019423896446824074,-0.010913330130279064,0.09083493798971176,0.057052984833717346,0.053897421807050705,0.02968442067503929,0.01682092808187008,-0.0473734475672245,-0.04326784610748291,-0.020668303593993187,-0.09364580363035202,0.027292029932141304,0.026699630543589592,0.08270170539617538,-0.0019637837540358305,-0.056955430656671524,0.0663859024643898,0.02924281917512417,-0.014903588220477104,-0.026133503764867783,0.033078085631132126,-0.012578188441693783,0.02650442160665989,-0.01001475378870964,-0.03540857136249542,0.018923230469226837,0.030105605721473694,-0.018976612016558647,0.04063315689563751,-0.006093418225646019,0.02502141334116459,-0.012332930229604244,-0.08086052536964417,4.161320684943348E-4,0.026283031329512596,-0.030388372018933296,-0.019412634894251823,-0.03689391165971756,0.018925093114376068,-0.03397482633590698,-0.07999216765165329,-0.04230911657214165,-0.025142911821603775,-0.006846408359706402,0.05908743292093277,-0.022003985941410065,0.040133245289325714,0.031386807560920715,-0.010388287715613842,0.05719384923577309,-0.005330725573003292,5.002868711017072E-4,-0.01610071398317814,-7.851929403841496E-4,-0.046576034277677536,0.03963885456323624);
		Vector v2 = Vector.of(-0.014672474935650826,-0.060921479016542435,0.10087049007415771,0.0153540950268507,0.03956206887960434,-0.07762990891933441,0.004401583690196276,-0.00851432979106903,-0.016627691686153412,-0.06950952857732773,0.01648535020649433,-0.021116597577929497,-0.06405960768461227,0.061415478587150574,-0.01646127924323082,0.03241392597556114,-0.0034250104799866676,0.08757651597261429,0.011445310898125172,-0.034614745527505875,0.03787026181817055,0.01969301886856556,-0.03634876385331154,-0.07617232948541641,-0.04989750683307648,0.018605932593345642,-0.0054916515946388245,0.029090989381074905,-0.01033382024616003,-0.0642751082777977,0.008981575258076191,0.03339681774377823,0.07036326825618744,-7.910212152637541E-4,0.025490863248705864,-0.03419310599565506,-0.02896084450185299,-0.04614390805363655,0.025531871244311333,-0.04316532984375954,-0.06870091706514359,-0.05159005895256996,-0.07589735835790634,-0.049602098762989044,0.02724762074649334,0.044040169566869736,0.030480124056339264,0.03235642611980438,0.03056095726788044,0.024091778323054314,-6.371267954818904E-4,0.02347361110150814,-0.002501036738976836,0.027920207008719444,-0.07511789351701736,0.012127351947128773,0.020146096125245094,0.006251493003219366,0.038035549223423004,-0.006672256626188755,0.031211068853735924,0.014829334802925587,-0.05150032788515091,-0.020497363060712814,0.04383742809295654,-0.03431278094649315,-0.03843933716416359,0.0150043535977602,-0.04187929630279541,0.059227555990219116,0.004107493907213211,-0.03497280925512314,-0.029103528708219528,0.003289624350145459,-0.08881937712430954,-0.008961683139204979,0.004717086907476187,0.02223982661962509,0.055549900978803635,-0.0161878764629364,0.024245597422122955,-0.01717694289982319,0.022717108950018883,0.06829257309436798,0.0635230541229248,0.004510267172008753,-0.06420350074768066,0.07498708367347717,0.07041779160499573,-0.06608670949935913,-0.043637268245220184,0.04252871870994568,0.013332311064004898,-0.008025133982300758,-0.030660146847367287,0.04573838785290718,-0.05960845202207565,0.020646514371037483,-0.05503839626908302,0.04121457040309906,0.08484749495983124,0.009258708916604519,0.01025861781090498,0.018627909943461418,-0.018932437524199486,0.008254818618297577,-0.02264932170510292,0.033970367163419724,0.06495217978954315,0.01382463425397873,0.0036916856188327074,-0.04240090772509575,-0.0912410244345665,-0.02600041963160038,0.02177176997065544,-0.014709528535604477,-0.02155478112399578,-0.04020718112587929,0.022628022357821465,-0.0989045575261116,0.009882892481982708,0.018138116225600243,0.03373369202017784,-0.06884878128767014,0.012620066292583942,-0.025955509394407272,-0.05680656433105469,0.0826617032289505,-0.05149027332663536,0.006291543133556843,0.025192342698574066,0.018419062718749046,-0.015112343244254589,0.023220116272568703,0.052635300904512405,0.005093525629490614,0.0545252189040184,0.039942674338817596,-0.019692622125148773,-0.03734058514237404,0.0588541179895401,-0.04563178867101669,0.04373469948768616,-0.04543791711330414,-0.05744180083274841,0.01720317266881466,-0.0854082927107811,-0.03623298183083534,-0.019212469458580017,0.05736434832215309,0.056226328015327454,0.004107156302779913,-0.027757612988352776,-0.03962142392992973,-0.04337155818939209,0.0029392745345830917,0.0712227076292038,-0.0540006048977375,0.01963852345943451,-0.05356534570455551,-0.052014924585819244,0.08905542641878128,0.017454756423830986,-0.011507829651236534,0.03509567677974701,0.0125507777556777,0.03138379380106926,0.024216271936893463,-0.050657689571380615,-0.025456245988607407,-0.01472635380923748,-0.08422835916280746,-0.09253020584583282,0.06076696142554283,-0.05572471395134926,0.013225755654275417,0.04623087868094444,0.01793598011136055,0.07384555786848068,0.008312879130244255,-0.0380166620016098,-0.046949807554483414,-0.05824628472328186,0.004187663085758686,-0.02620762400329113,-0.017816975712776184,0.03054753504693508,0.009359095245599747,0.011434297077357769,0.04951866343617439,-0.05787675082683563,0.03459971770644188,-0.07160191982984543,0.0027130518574267626,0.04751984775066376,-0.010564509779214859,0.08280697464942932,0.0769881084561348,-0.0314154289662838,0.04879987612366676,-0.013131959363818169,0.02883918583393097,-0.03760639578104019,-0.028443802148103714,-0.006969589274376631,-0.0415022112429142,0.018564512953162193,0.0040444862097501755,0.020479293540120125,-0.014797110110521317,0.048078637570142746,-0.07342076301574707,0.040382057428359985,0.06267718225717545,-0.03097708337008953,0.08996868133544922,0.026140186935663223,0.006135874427855015,0.10458917915821075,-0.0499354749917984,0.05887168273329735,0.0037474457640200853,-0.07183420658111572,-0.09926125407218933,-0.01864713430404663,-0.0078071593306958675,-0.009292719885706902,0.03932597488164902,0.06689969450235367,0.05718696862459183,0.04907668009400368,0.013830272480845451,-0.02062700130045414,0.06239774078130722,0.05830024927854538,-0.09595858305692673,0.027675090357661247,-0.033106349408626556,0.026968302205204964,0.054775115102529526,-0.03282015025615692,0.004955218639224768,-0.02857114002108574,-0.0373460091650486,0.06969446688890457,-0.029710909351706505,-0.03563770279288292,0.04336013272404671,-0.041999753564596176,-0.11235130578279495,0.027443813160061836,-0.01919204369187355,-5.070360493846238E-4,0.0020254417322576046,-0.043836191296577454,0.048074278980493546,-0.07363380491733551,0.07570996135473251,-0.01560026966035366,-0.062470607459545135,0.006533526815474033,0.003846443723887205,-0.058247700333595276,-0.03624064475297928,0.055345263332128525,0.036538902670145035,0.08495946228504181,-0.0964236706495285,0.006666433997452259,0.07158039510250092,0.0941976010799408,-0.02965710312128067,-0.04879981651902199,0.024249060079455376,0.04123895987868309,0.03856048732995987,-0.018006760627031326,-0.07077029347419739,0.034668929874897,-0.001403667381964624,0.08379256725311279,0.0028605484403669834,-0.013252174481749535,0.01622060313820839,-0.041202399879693985,-0.01771884225308895,-0.05962781980633736,0.05389392748475075,0.035289160907268524,-0.03918454051017761,0.011331198737025261,0.04514545947313309,-0.04957684874534607,-0.07102601230144501,0.035673972219228745,-0.008051143027842045,-0.06581827998161316,0.053875528275966644,-0.027136625722050667,0.011802584864199162,-0.012990973889827728,-0.07318586111068726,0.006141304038465023,-0.004976529162377119,0.017841869965195656,0.019139785319566727,-0.023918459191918373,0.03851857781410217,-0.05198444053530693,0.023321101441979408,0.008677342906594276,-0.057300787419080734,-0.04280127212405205,-0.04708677530288696,0.030500398948788643,-0.054328154772520065,-0.042221568524837494,0.046578165143728256,-0.028197424486279488,-0.05499295890331268,-0.010021836496889591,0.017890794202685356,0.03439932316541672,-0.026512403041124344,-0.0027062806766480207,0.02413947321474552,-0.018246743828058243,-0.010374336503446102,0.031514525413513184,0.08428751677274704,-0.017485179007053375,-0.04065853729844093,-0.028235774487257004,0.08206970989704132,0.012247757986187935,0.02642357163131237,0.06565714627504349,-0.06611126661300659,0.014827042818069458,0.09523377567529678,-0.013245407491922379,-0.024334093555808067,0.014618813060224056,-0.043163593858480453,-0.005913678091019392,-0.031274858862161636,-0.013277742080390453,0.047811511904001236,0.04316306859254837,0.06522604823112488,0.08264916390180588,0.013731591403484344,-0.04609978199005127,-0.056467678397893906,0.009641322307288647,0.014174544252455235,0.0187104232609272,0.0374431349337101,0.035957034677267075,0.021461954340338707,0.004401089157909155,-0.03889656439423561,-0.04913324490189552,-0.016310976818203926,-0.02966526336967945,0.01092937309294939,-0.05317120999097824,0.02729460783302784,-0.06934302300214767,-0.03283608332276344,0.012706915847957134,-0.07476099580526352,-0.07970266789197922,0.01850113272666931,0.035026825964450836,0.011481347493827343,-0.06983351707458496,0.06622762978076935,-0.004287383519113064,0.02485404536128044,0.013271123170852661,-0.009022021666169167,-0.05084630846977234,0.033457107841968536,-0.03561608865857124,0.003862060373649001,0.023318462073802948,-0.013211940415203571,-0.035802725702524185,-0.031690169125795364,0.11517245322465897,0.027899883687496185,0.09763495624065399,-0.01473498810082674,0.014655828475952148,-0.03888535499572754,0.05403246730566025,-0.01882738061249256,0.042323723435401917,-0.07802210748195648,-0.03363383561372757,-0.013654928654432297,0.0669025406241417,-0.006674893666058779,0.058964330703020096,0.07374133169651031,9.022817830555141E-4,-0.022628292441368103,0.016833335161209106,-0.029718732461333275,0.09526200592517853,0.017205052077770233,-0.03896680846810341,-0.042477115988731384,-0.016174279153347015,-0.007498868275433779,-0.010973247699439526,0.016591034829616547,0.007044659461826086,-0.026406990364193916,-0.026990795508027077,-0.01155818346887827,0.07962888479232788,0.02354908175766468,0.0635179951786995,0.049256835132837296,0.07857359200716019,-0.056579213589429855,-0.05932610109448433,0.006638250779360533,-0.0982515886425972,0.0035712309181690216,-0.028197873383760452,0.04056137055158615,0.0012351424666121602,0.02043057791888714,0.04588925838470459,-0.015251804143190384,0.04060744121670723,-0.025710562244057655,-0.03255010023713112,-0.07972458750009537,-0.021347513422369957,-0.047134578227996826,0.051518622785806656,0.14302733540534973,0.009764489717781544,-0.02981240302324295,0.020388782024383545,-0.006143294274806976,0.037423934787511826,-0.027064388617873192,0.01941872574388981,-0.014085566624999046,-0.004230785649269819,0.03489984944462776,0.019423896446824074,-0.010913330130279064,0.09083493798971176,0.057052984833717346,0.053897421807050705,0.02968442067503929,0.01682092808187008,-0.0473734475672245,-0.04326784610748291,-0.020668303593993187,-0.09364580363035202,0.027292029932141304,0.026699630543589592,0.08270170539617538,-0.0019637837540358305,-0.056955430656671524,0.0663859024643898,0.02924281917512417,-0.014903588220477104,-0.026133503764867783,0.033078085631132126,-0.012578188441693783,0.02650442160665989,-0.01001475378870964,-0.03540857136249542,0.018923230469226837,0.030105605721473694,-0.018976612016558647,0.04063315689563751,-0.006093418225646019,0.02502141334116459,-0.012332930229604244,-0.08086052536964417,4.161320684943348E-4,0.026283031329512596,-0.030388372018933296,-0.019412634894251823,-0.03689391165971756,0.018925093114376068,-0.03397482633590698,-0.07999216765165329,-0.04230911657214165,-0.025142911821603775,-0.006846408359706402,0.05908743292093277,-0.022003985941410065,0.040133245289325714,0.031386807560920715,-0.010388287715613842,0.05719384923577309,-0.005330725573003292,5.002868711017072E-4,-0.01610071398317814,-7.851929403841496E-4,-0.046576034277677536,0.03963885456323624);

		assertEquals(0.0, v1.angle(v2));
	}
}