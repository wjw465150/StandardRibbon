package org.wjw.standardribbon;

import org.wjw.standardribbon.model.User;
import org.wjw.standardribbon.service.RemoteService;

import com.netflix.client.ClientFactory;
import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.ZoneAwareLoadBalancer;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.ribbon.LBClient;
import feign.ribbon.LBClientFactory;
import feign.ribbon.RibbonClient;

public class StdRibbon {
	public static void main(String[] args) throws Exception {
		ConfigurationManager.loadPropertiesFromResources("sample-client.properties");

		RibbonClient client = RibbonClient.builder().lbClientFactory(new LBClientFactory() {
			@Override
			public LBClient create(String clientName) {
				IClientConfig config = ClientFactory.getNamedConfig(clientName);
				ILoadBalancer lb = ClientFactory.getNamedLoadBalancer(clientName);
				ZoneAwareLoadBalancer zb = (ZoneAwareLoadBalancer) lb;
				zb.setRule(new RandomRule());
				return LBClient.create(lb, config);
			}
		}).build();

		/*
		  重点看ribbon.properties文件里的service-one.ribbon.listOfServers配置项,该配置项指定了服务生产端的真实地址.
                       与RemoteService接口绑定的URL地址是"http://service-one/",
                       在调用时会被替换为http://192.168.2.113:8861/或http://192.168.2.114:8861/,再与接口中@RequestLine指定的地址进行拼接,得到最终请求地址
                      本例中最终请求地址为"http://192.168.2.113:8861/add"或"http://192.168.2.114:8861/add"
		*/
		RemoteService service = Feign.builder().client(client).encoder(new JacksonEncoder())
				.decoder(new JacksonDecoder()).target(RemoteService.class, "http://service-one/");

		/**
		 * 调用测试
		 */
		User user=new User();
		user.setUsername("hello");
		for (int i = 1; i <= 10; i++) {
			int result = service.getAdd(1, 2);
			System.out.println("result:" + result);
			
			User userB=service.getOwner(user);
			System.out.println("user:" + userB);
		}
	}
}
