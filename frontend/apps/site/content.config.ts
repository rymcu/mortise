import { defineCollection, z } from '@nuxt/content'

export const collections = {
  home: defineCollection({
    source: 'home.yml',
    type: 'data',
    schema: z.object({
      seo: z.object({
        title: z.string(),
        description: z.string()
      }).optional(),
      title: z.string(),
      description: z.string(),
      hero: z.object({
        links: z.array(z.object({
          label: z.string(),
          to: z.string(),
          icon: z.string().optional(),
          trailing: z.boolean().optional(),
          color: z.enum(['primary', 'secondary', 'neutral', 'error', 'warning', 'success', 'info']).optional(),
          variant: z.enum(['solid', 'outline', 'subtle', 'soft', 'ghost', 'link']).optional(),
          size: z.enum(['xs', 'sm', 'md', 'lg', 'xl']).optional(),
          target: z.enum(['_blank', '_self']).optional()
        }))
      }),
      features: z.object({
        title: z.string(),
        description: z.string(),
        items: z.array(z.object({
          title: z.string(),
          description: z.string(),
          icon: z.string()
        }))
      }),
      steps: z.object({
        title: z.string(),
        description: z.string(),
        items: z.array(z.object({
          title: z.string(),
          description: z.string(),
          icon: z.string().optional()
        }))
      }),
      cta: z.object({
        title: z.string(),
        description: z.string(),
        links: z.array(z.object({
          label: z.string(),
          to: z.string(),
          color: z.enum(['primary', 'secondary', 'neutral', 'error', 'warning', 'success', 'info']).optional(),
          variant: z.enum(['solid', 'outline', 'subtle', 'soft', 'ghost', 'link']).optional(),
          icon: z.string().optional(),
          target: z.enum(['_blank', '_self']).optional()
        }))
      }).optional()
    })
  }),
  docs: defineCollection({
    source: 'docs/**/*.md',
    type: 'page',
    schema: z.object({
      title: z.string(),
      description: z.string().optional(),
      order: z.number().optional()
    })
  }),
  download: defineCollection({
    source: 'download.yml',
    type: 'data',
    schema: z.object({
      title: z.string(),
      description: z.string(),
      items: z.array(z.object({
        platform: z.string(),
        icon: z.string(),
        version: z.string(),
        url: z.string(),
        description: z.string().optional()
      }))
    })
  }),
  about: defineCollection({
    source: 'about.yml',
    type: 'data',
    schema: z.object({
      title: z.string(),
      description: z.string(),
      mission: z.string().optional(),
      license: z.object({
        type: z.string(),
        url: z.string()
      }).optional(),
      github: z.string().optional(),
      team: z.array(z.object({
        name: z.string(),
        role: z.string(),
        avatar: z.string().optional(),
        github: z.string().optional()
      })).optional()
    })
  })
}
